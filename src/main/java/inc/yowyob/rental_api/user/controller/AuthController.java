package inc.yowyob.rental_api.user.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.dto.*;
import inc.yowyob.rental_api.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs d'authentification et de gestion des utilisateurs")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Connexion utilisateur",
        description = "Authentifie un utilisateur avec email et mot de passe"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Connexion réussie"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données d'entrée invalides"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Email ou mot de passe incorrect"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "423",
            description = "Compte temporairement verrouillé"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
        @Parameter(description = "Informations de connexion")
        @Valid @RequestBody LoginRequestDto loginRequest,
        HttpServletRequest request
    ) {
        log.info("POST /auth/login - Login attempt for email: {}", loginRequest.getEmail());

        try {
            String ipAddress = getClientIpAddress(request);
            AuthResponseDto authResponse = authService.login(loginRequest, ipAddress);

            log.info("Successful login for user: {}", loginRequest.getEmail());

            return ApiResponseUtil.success(
                authResponse,
                "Connexion réussie"
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            return ApiResponseUtil.unauthorized("Email ou mot de passe incorrect");
        } catch (DisabledException e) {
            log.warn("Login attempt for disabled account: {}", loginRequest.getEmail());
            return ApiResponseUtil.error(e.getMessage(), 423);
        } catch (Exception e) {
            log.error("Error during login for email: {}", loginRequest.getEmail(), e);
            return ApiResponseUtil.error("Erreur lors de la connexion", 500);
        }
    }

    @Operation(
        summary = "Inscription utilisateur",
        description = "Crée un nouveau compte utilisateur"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Compte créé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données d'entrée invalides"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email déjà utilisé"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
        @Parameter(description = "Informations d'inscription")
        @Valid @RequestBody RegisterRequestDto registerRequest
    ) {
        log.info("POST /auth/register - Registration attempt for email: {}", registerRequest.getEmail());

        try {
            AuthResponseDto authResponse = authService.register(registerRequest);

            log.info("Successful registration for user: {}", registerRequest.getEmail());

            return ApiResponseUtil.created(
                authResponse,
                "Compte créé avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email: {} - {}", registerRequest.getEmail(), e.getMessage());
            return ApiResponseUtil.conflict(e.getMessage());
        } catch (Exception e) {
            log.error("Error during registration for email: {}", registerRequest.getEmail(), e);
            return ApiResponseUtil.error("Erreur lors de la création du compte", 500);
        }
    }

    @Operation(
        summary = "Rafraîchir le token",
        description = "Génère un nouveau token d'accès à partir du refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
        @Parameter(description = "Refresh token")
        @Valid @RequestBody RefreshTokenRequestDto refreshRequest
    ) {
        log.debug("POST /auth/refresh - Token refresh request");

        try {
            AuthResponseDto authResponse = authService.refreshToken(refreshRequest.getRefreshToken());

            return ApiResponseUtil.success(
                authResponse,
                "Token rafraîchi avec succès"
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid refresh token provided");
            return ApiResponseUtil.unauthorized("Token de rafraîchissement invalide");
        } catch (DisabledException e) {
            log.warn("Refresh token request for disabled account");
            return ApiResponseUtil.forbidden("Compte non actif");
        } catch (Exception e) {
            log.error("Error during token refresh", e);
            return ApiResponseUtil.error("Erreur lors du rafraîchissement du token", 500);
        }
    }

    @Operation(
        summary = "Changer le mot de passe",
        description = "Change le mot de passe de l'utilisateur connecté"
    )
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @Parameter(description = "Informations de changement de mot de passe")
        @Valid @RequestBody ChangePasswordRequestDto changePasswordRequest,
        HttpServletRequest request
    ) {
        log.info("POST /auth/change-password - Password change request");

        try {
            // TODO: Récupérer l'ID utilisateur depuis le token JWT
             UUID userId = SecurityUtils.getCurrentUserId();
             authService.changePassword(userId, changePasswordRequest.getCurrentPassword(),
                                      changePasswordRequest.getNewPassword());

            return ApiResponseUtil.success(
                null,
                "Mot de passe changé avec succès"
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid current password provided");
            return ApiResponseUtil.badRequest("Mot de passe actuel incorrect");
        } catch (Exception e) {
            log.error("Error during password change", e);
            return ApiResponseUtil.error("Erreur lors du changement de mot de passe", 500);
        }
    }

    @Operation(
        summary = "Déconnexion",
        description = "Déconnecte l'utilisateur (invalide le token côté client)"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("POST /auth/logout - Logout request");

        // Note: Avec JWT stateless, la déconnexion se fait côté client
        // En supprimant le token du storage local

        return ApiResponseUtil.success(
            null,
            "Déconnexion réussie"
        );
    }

    /**
     * Extrait l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
