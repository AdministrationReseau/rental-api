package inc.yowyob.rental_api.user.service;

import inc.yowyob.rental_api.config.AppProperties;
import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.security.jwt.JwtTokenProvider;
import inc.yowyob.rental_api.user.dto.*;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AppProperties appProperties;

    /**
     * Authentifie un utilisateur
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest, String ipAddress) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Vérifier si le compte est verrouillé
        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", loginRequest.getEmail());
            throw new DisabledException("Account is temporarily locked due to failed login attempts");
        }

        // Vérifier si l'utilisateur peut se connecter
        if (!user.canLogin()) {
            log.warn("Login attempt for inactive account: {}", loginRequest.getEmail());
            throw new DisabledException("Account is not active");
        }

        try {
            // Authentifier
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Mettre à jour les informations de connexion
            user.updateLastLogin(ipAddress);
            userRepository.save(user);

            // Générer les tokens
            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            log.info("Successful login for user: {}", user.getEmail());

            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            // Incrémenter les tentatives échouées
            user.incrementFailedLoginAttempts();
            userRepository.save(user);

            log.warn("Failed login attempt for email: {} (attempts: {})",
                loginRequest.getEmail(), user.getFailedLoginAttempts());

            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Enregistre un nouvel utilisateur
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Créer l'utilisateur
        User user = new User(
            registerRequest.getEmail(),
            passwordEncoder.encode(registerRequest.getPassword()),
            registerRequest.getFirstName(),
            registerRequest.getLastName(),
            registerRequest.getUserType()
        );

        user.setPhone(registerRequest.getPhone());

        // Activer directement pour les clients, en attente pour les propriétaires
        if (registerRequest.getUserType().getCode().equals("client")) {
            user.setStatus(UserStatus.ACTIVE);
        }

        User savedUser = userRepository.save(user);

        // Générer les tokens
        String accessToken = jwtTokenProvider.generateToken(savedUser);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

        log.info("Successful registration for user: {}", savedUser.getEmail());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Rafraîchit le token d'accès
     */
    public AuthResponseDto refreshToken(String refreshToken) {
        log.debug("Refresh token request");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.canLogin()) {
            throw new DisabledException("Account is not active");
        }

        // Générer de nouveaux tokens
        String newAccessToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.debug("Token refreshed for user: {}", user.getEmail());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Password change request for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Construit la réponse d'authentification
     */
    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(appProperties.getJwt().getExpiration() / 1000) // en secondes
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .userType(user.getUserType().getCode())
            .status(user.getStatus().getCode())
            .organizationId(user.getOrganizationId())
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
