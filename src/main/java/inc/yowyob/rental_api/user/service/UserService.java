package inc.yowyob.rental_api.user.service;

import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.user.dto.*;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouvel utilisateur avec organisation spécifiée
     */
    @Transactional
    public UserDto createUser(RegisterRequestDto registerDto, UUID organizationId) {
        log.info("Creating user: {} for organization: {}", registerDto.getEmail(), organizationId);

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Vérifier le téléphone s'il est fourni
        if (registerDto.getPhone() != null && userRepository.existsByPhone(registerDto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setPhone(registerDto.getPhone());
        user.setUserType(registerDto.getUserType());
        user.setOrganizationId(organizationId);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToDto(savedUser);
    }

    /**
     * Récupère les utilisateurs par type avec filtres
     */
    public List<UserDto> getUsersByType(UserType userType, UUID organizationId, UUID agencyId,
                                        int page, int size, String search, Boolean isActive) {
        log.debug("Fetching users by type: {}, org: {}, agency: {}", userType, organizationId, agencyId);

        // Construire les critères de recherche
        UUID targetOrgId = organizationId != null ? organizationId : SecurityUtils.getCurrentUserOrganizationId();

        if (targetOrgId == null) {
            return List.of();
        }

        // Récupérer les utilisateurs selon les critères
        List<User> users = userRepository.findByOrganizationIdAndUserType(targetOrgId, userType);

        return users.stream()
            .filter(user -> agencyId == null || (user.getAgencyId() != null && user.getAgencyId().equals(agencyId)))
            .filter(user -> isActive == null || user.getStatus() == (isActive ? UserStatus.ACTIVE : UserStatus.INACTIVE))
            .filter(user -> search == null || userMatchesSearch(user, search))
            .filter(this::canAccessUser)
            .skip((long) page * size)
            .limit(size)
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Met à jour un utilisateur
     */
    @Transactional
    public UserDto updateUser(UUID userId, UpdateUserDto updateDto) {
        log.info("Updating user: {}", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        // Appliquer les mises à jour
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhone() != null && !updateDto.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(updateDto.getPhone())) {
                throw new IllegalArgumentException("Phone number already exists");
            }
            user.setPhone(updateDto.getPhone());
            user.setPhoneVerified(false); // Réinitialiser la vérification
        }
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(updateDto.getEmail());
            user.setEmailVerified(false); // Réinitialiser la vérification
        }

        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    /**
     * Supprime un utilisateur
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        // Vérifier si l'utilisateur peut être supprimé
        if (user.getStatus() == UserStatus.ACTIVE && user.getLastLoginAt() != null &&
            user.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(30))) {
            throw new IllegalStateException("Cannot delete user with recent activity");
        }

        // Marquer comme supprimé plutôt que supprimer définitivement
        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User marked as deleted: {}", userId);
    }

    /**
     * Active/désactive un utilisateur
     */
    @Transactional
    public UserDto toggleUserStatus(UUID userId, boolean isActive) {
        log.info("Toggling user status: {} to {}", userId, isActive);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        UserStatus newStatus = isActive ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());

        // Si on désactive, réinitialiser les tentatives de connexion
        if (!isActive) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     */
    @Transactional
    public String resetUserPassword(UUID userId) {
        log.info("Resetting password for user: {}", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        // Générer un mot de passe temporaire
        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Password reset successfully for user: {}", userId);

        return temporaryPassword;
    }

    /**
     * Assigne un utilisateur à une agence
     */
    @Transactional
    public UserDto assignUserToAgency(UUID userId, UUID agencyId) {
        log.info("Assigning user {} to agency {}", userId, agencyId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        // Vérifier que l'utilisateur est du type STAFF
        if (user.getUserType() != UserType.STAFF) {
            throw new IllegalArgumentException("Only staff users can be assigned to agencies");
        }

        user.setAgencyId(agencyId);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    /**
     * Retire un utilisateur d'une agence
     */
    @Transactional
    public UserDto removeUserFromAgency(UUID userId) {
        log.info("Removing user {} from agency", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        user.setAgencyId(null);
        user.setEmployeeId(null);
        user.setDepartment(null);
        user.setPosition(null);
        user.setSupervisorId(null);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    /**
     * Définit les informations employé
     */
    @Transactional
    public UserDto setEmployeeInfo(UUID userId, String employeeId, String department,
                                   String position, UUID supervisorId) {
        log.info("Setting employee info for user: {}", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        user.setEmployeeId(employeeId);
        user.setDepartment(department);
        user.setPosition(position);
        user.setSupervisorId(supervisorId);

        if (user.getHiredAt() == null) {
            user.setHiredAt(LocalDateTime.now());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    /**
     * Récupère un utilisateur par ID
     */
    public UserDto getUserById(UUID userId) {
        log.debug("Fetching user: {}", userId);

        User user = getUserOrThrow(userId);
        validateUserAccess(user);

        return mapToDto(user);
    }

    /**
     * Vérifie si l'utilisateur connecté peut créer du personnel dans une organisation
     */
    public boolean canCreateStaff(UUID currentUserId, UUID organizationId) {
        try {
            // Super admin peut tout faire
            if (SecurityUtils.isCurrentUserSuperAdmin()) {
                return true;
            }

            // Propriétaire peut créer du personnel dans son organisation
            if (SecurityUtils.isCurrentUserOwner()) {
                UUID userOrgId = SecurityUtils.getCurrentUserOrganizationId();
                return organizationId.equals(userOrgId);
            }

            // Utilisateur avec permission USER_WRITE peut créer du personnel
            return SecurityUtils.getCurrentUser().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER_WRITE"));

        } catch (Exception e) {
            log.error("Error checking staff creation permissions", e);
            return false;
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère un utilisateur ou lance une exception
     */
    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /**
     * Valide l'accès à un utilisateur
     */
    private void validateUserAccess(User user) {
        if (!canAccessUser(user)) {
            throw new SecurityException("Access denied to user");
        }
    }

    /**
     * Vérifie si l'utilisateur connecté peut accéder à cet utilisateur
     */
    private boolean canAccessUser(User user) {
        // Super admin peut tout voir
        if (SecurityUtils.isCurrentUserSuperAdmin()) {
            return true;
        }

        // Propriétaire peut voir les utilisateurs de son organisation
        if (SecurityUtils.isCurrentUserOwner()) {
            UUID currentUserOrgId = SecurityUtils.getCurrentUserOrganizationId();
            return user.getOrganizationId() != null && user.getOrganizationId().equals(currentUserOrgId);
        }

        // Staff peut voir les utilisateurs de son organisation et son propre profil
        if (SecurityUtils.isCurrentUserStaff()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            UUID currentUserOrgId = SecurityUtils.getCurrentUserOrganizationId();

            return user.getId().equals(currentUserId) ||
                (user.getOrganizationId() != null && user.getOrganizationId().equals(currentUserOrgId));
        }

        // Client peut voir son propre profil
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return user.getId().equals(currentUserId);
    }

    /**
     * Vérifie si un utilisateur correspond aux critères de recherche
     */
    private boolean userMatchesSearch(User user, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }

        String searchLower = search.toLowerCase();
        return user.getFirstName().toLowerCase().contains(searchLower) ||
            user.getLastName().toLowerCase().contains(searchLower) ||
            user.getEmail().toLowerCase().contains(searchLower) ||
            (user.getEmployeeId() != null && user.getEmployeeId().toLowerCase().contains(searchLower)) ||
            (user.getPhone() != null && user.getPhone().contains(search));
    }

    /**
     * Génère un mot de passe temporaire
     */
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Assurer la présence d'au moins une majuscule, une minuscule, un chiffre et un caractère spécial
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(26)));
        password.append("abcdefghijklmnopqrstuvwxyz".charAt(random.nextInt(26)));
        password.append("0123456789".charAt(random.nextInt(10)));
        password.append("!@#$%^&*".charAt(random.nextInt(8)));

        // Compléter avec des caractères aléatoires
        for (int i = 4; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Mélanger les caractères
        for (int i = 0; i < password.length(); i++) {
            int randomIndex = random.nextInt(password.length());
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomIndex));
            password.setCharAt(randomIndex, temp);
        }

        return password.toString();
    }

    /**
     * Mappe une entité User vers UserDto
     */
    private UserDto mapToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .userType(user.getUserType())
            .userStatus(user.getStatus())
            .organizationId(user.getOrganizationId())
            .agencyId(user.getAgencyId())
            .employeeId(user.getEmployeeId())
            .department(user.getDepartment())
            .position(user.getPosition())
            .supervisorId(user.getSupervisorId())
            .isActive(user.getStatus() == UserStatus.ACTIVE)
            .mustChangePassword(user.getMustChangePassword())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .hiredAt(user.getHiredAt())
            .fullName(user.getFirstName() + " " + user.getLastName())
            .hasActiveRoles(false)
            .roleCount(0)
            .build();
    }
}
