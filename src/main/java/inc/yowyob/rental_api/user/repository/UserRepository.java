package inc.yowyob.rental_api.user.repository;

import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.user.entities.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {

    /**
     * Trouve un utilisateur par email
     */
    @Query("SELECT * FROM users WHERE email = ?0 ALLOW FILTERING")
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà dans le système
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = ?0 ALLOW FILTERING")
    Long countByEmail(String email);

    /**
     * Vérifie si un email existe (version optimisée)
     */
    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }

    /**
     * Trouve un utilisateur par téléphone
     */
    @Query("SELECT * FROM users WHERE phone = ?0 ALLOW FILTERING")
    Optional<User> findByPhone(String phone);

    /**
     * Vérifie si un téléphone existe déjà
     */
    @Query("SELECT COUNT(*) FROM users WHERE phone = ?0 ALLOW FILTERING")
    Long countByPhone(String phone);

    /**
     * Vérifie si un téléphone existe
     */
    default boolean existsByPhone(String phone) {
        return countByPhone(phone) > 0;
    }

    /**
     * Trouve les utilisateurs par organisation
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 ALLOW FILTERING")
    List<User> findByOrganizationId(UUID organizationId);

    /**
     * Trouve les utilisateurs par type
     */
    @Query("SELECT * FROM users WHERE user_type = ?0 ALLOW FILTERING")
    List<User> findByUserType(UserType userType);

    /**
     * Trouve les utilisateurs par statut
     */
    @Query("SELECT * FROM users WHERE status = ?0 ALLOW FILTERING")
    List<User> findByStatus(UserStatus status);

    /**
     * Trouve les propriétaires d'organisation (OWNER)
     */
    @Query("SELECT * FROM users WHERE user_type = 'OWNER' ALLOW FILTERING")
    List<User> findAllOwners();

    /**
     * Trouve les clients (CLIENT)
     */
    @Query("SELECT * FROM users WHERE user_type = 'CLIENT' ALLOW FILTERING")
    List<User> findAllClients();

    /**
     * Trouve les utilisateurs actifs
     */
    @Query("SELECT * FROM users WHERE status = 'ACTIVE' ALLOW FILTERING")
    List<User> findActiveUsers();

    /**
     * Trouve un utilisateur par token de vérification email
     */
    @Query("SELECT * FROM users WHERE email_verification_token = ?0 ALLOW FILTERING")
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Trouve un utilisateur par token de reset password
     */
    @Query("SELECT * FROM users WHERE password_reset_token = ?0 ALLOW FILTERING")
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Trouve les utilisateurs créés entre deux dates
     */
    @Query("SELECT * FROM users WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les utilisateurs avec des tentatives de connexion échouées
     */
    @Query("SELECT * FROM users WHERE failed_login_attempts > 0 ALLOW FILTERING")
    List<User> findUsersWithFailedLoginAttempts();

    /**
     * Trouve les utilisateurs verrouillés
     */
    @Query("SELECT * FROM users WHERE locked_until > ?0 ALLOW FILTERING")
    List<User> findLockedUsers(LocalDateTime now);

    /**
     * Trouve les utilisateurs avec email non vérifié
     */
    @Query("SELECT * FROM users WHERE email_verified = false ALLOW FILTERING")
    List<User> findUsersWithUnverifiedEmail();

    /**
     * Trouve les utilisateurs avec téléphone non vérifié
     */
    @Query("SELECT * FROM users WHERE phone_verified = false ALLOW FILTERING")
    List<User> findUsersWithUnverifiedPhone();

    /**
     * Trouve les utilisateurs par organisation et type
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 AND user_type = ?1 ALLOW FILTERING")
    List<User> findByOrganizationIdAndUserType(UUID organizationId, UserType userType);

    /**
     * Trouve les propriétaires par organisation (normalement un seul)
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 AND user_type = 'OWNER' ALLOW FILTERING")
    List<User> findOwnersByOrganizationId(UUID organizationId);

    /**
     * Trouve le personnel d'une organisation
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 AND user_type = 'STAFF' ALLOW FILTERING")
    List<User> findStaffByOrganizationId(UUID organizationId);

    /**
     * Trouve les utilisateurs connectés récemment
     */
    @Query("SELECT * FROM users WHERE last_login_at >= ?0 ALLOW FILTERING")
    List<User> findRecentlyLoggedInUsers(LocalDateTime since);

    /**
     * Compte les utilisateurs par type
     */
    @Query("SELECT COUNT(*) FROM users WHERE user_type = ?0 ALLOW FILTERING")
    Long countByUserType(UserType userType);

    /**
     * Compte les utilisateurs par statut
     */
    @Query("SELECT COUNT(*) FROM users WHERE status = ?0 ALLOW FILTERING")
    Long countByStatus(UserStatus status);

    /**
     * Compte les utilisateurs par organisation
     */
    @Query("SELECT COUNT(*) FROM users WHERE organization_id = ?0 ALLOW FILTERING")
    Long countByOrganizationId(UUID organizationId);

    /**
     * Trouve les utilisateurs créés aujourd'hui
     */
    @Query("SELECT * FROM users WHERE created_at >= ?0 ALLOW FILTERING")
    List<User> findCreatedSince(LocalDateTime since);

    /**
     * Trouve les super administrateurs
     */
    @Query("SELECT * FROM users WHERE user_type = 'SUPER_ADMIN' ALLOW FILTERING")
    List<User> findSuperAdmins();

    /**
     * Trouve les utilisateurs par agence
     */
    @Query("SELECT * FROM users WHERE agency_id = ?0 ALLOW FILTERING")
    List<User> findByAgencyId(UUID agencyId);

    /**
     * Trouve les utilisateurs par organisation, type et agence
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 AND user_type = ?1 AND agency_id = ?2 ALLOW FILTERING")
    List<User> findByOrganizationIdAndUserTypeAndAgencyId(UUID organizationId, UserType userType, UUID agencyId);

    /**
     * Trouve les utilisateurs STAFF d'une agence
     */
    @Query("SELECT * FROM users WHERE agency_id = ?0 AND user_type = 'STAFF' ALLOW FILTERING")
    List<User> findStaffByAgencyId(UUID agencyId);

    /**
     * Compte les utilisateurs par type dans une organisation
     */
    @Query("SELECT COUNT(*) FROM users WHERE organization_id = ?0 AND user_type = ?1 ALLOW FILTERING")
    Long countByOrganizationIdAndUserType(UUID organizationId, UserType userType);

    /**
     * Compte les utilisateurs actifs par type dans une organisation
     */
    @Query("SELECT COUNT(*) FROM users WHERE organization_id = ?0 AND user_type = ?1 AND status = 'ACTIVE' ALLOW FILTERING")
    Long countActiveByOrganizationIdAndUserType(UUID organizationId, UserType userType);

    /**
     * Trouve les superviseurs potentiels (STAFF avec position de management)
     */
    @Query("SELECT * FROM users WHERE organization_id = ?0 AND user_type = 'STAFF' AND position LIKE '%manager%' OR position LIKE '%supervisor%' ALLOW FILTERING")
    List<User> findPotentialSupervisorsByOrganizationId(UUID organizationId);
}
