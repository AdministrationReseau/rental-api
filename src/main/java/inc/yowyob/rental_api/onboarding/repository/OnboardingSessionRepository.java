package inc.yowyob.rental_api.onboarding.repository;

import inc.yowyob.rental_api.core.enums.OnboardingStatus;
import inc.yowyob.rental_api.onboarding.entities.OnboardingSession;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingSessionRepository extends CassandraRepository<OnboardingSession, UUID> {

    /**
     * Trouve la session active d'un utilisateur (si elle existe)
     * Note: Peut retourner vide car l'utilisateur n'existe pas forcément au début
     */
    @Query("SELECT * FROM onboarding_sessions WHERE user_id = ?0 AND status = 'IN_PROGRESS' ALLOW FILTERING")
    Optional<OnboardingSession> findActiveByUserId(UUID userId);

    /**
     * Trouve toutes les sessions d'un utilisateur
     */
    @Query("SELECT * FROM onboarding_sessions WHERE user_id = ?0 ALLOW FILTERING")
    List<OnboardingSession> findAllByUserId(UUID userId);

    /**
     * Trouve les sessions par statut
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = ?0 ALLOW FILTERING")
    List<OnboardingSession> findAllByStatus(OnboardingStatus status);

    /**
     * Trouve les sessions expirées qui sont encore marquées comme en cours
     */
    @Query("SELECT * FROM onboarding_sessions WHERE expires_at < ?0 AND status = 'IN_PROGRESS' ALLOW FILTERING")
    List<OnboardingSession> findExpiredSessions(LocalDateTime now);

    /**
     * Trouve les sessions créées dans une période donnée
     */
    @Query("SELECT * FROM onboarding_sessions WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<OnboardingSession> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les sessions terminées avec succès
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'COMPLETED' ALLOW FILTERING")
    List<OnboardingSession> findCompletedSessions();

    /**
     * Trouve les sessions terminées avec succès dans une période
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'COMPLETED' AND completed_at >= ?0 AND completed_at <= ?1 ALLOW FILTERING")
    List<OnboardingSession> findCompletedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Compte les sessions par statut
     */
    @Query("SELECT COUNT(*) FROM onboarding_sessions WHERE status = ?0 ALLOW FILTERING")
    Long countByStatus(OnboardingStatus status);

    /**
     * Trouve les sessions en cours depuis plus de X heures (sessions "stales")
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'IN_PROGRESS' AND created_at < ?0 ALLOW FILTERING")
    List<OnboardingSession> findStaleInProgressSessions(LocalDateTime cutoffTime);

    /**
     * Trouve les sessions par organisation créée
     */
    @Query("SELECT * FROM onboarding_sessions WHERE created_organization_id = ?0 ALLOW FILTERING")
    List<OnboardingSession> findByCreatedOrganizationId(UUID organizationId);

    /**
     * Trouve les sessions sans utilisateur associé (sessions en début de processus)
     */
    @Query("SELECT * FROM onboarding_sessions WHERE user_id IS NULL AND status = 'IN_PROGRESS' ALLOW FILTERING")
    List<OnboardingSession> findSessionsWithoutUser();

    /**
     * Trouve les sessions qui ont des informations propriétaire mais pas d'utilisateur créé
     */
    @Query("SELECT * FROM onboarding_sessions WHERE user_id IS NULL AND owner_info_data IS NOT NULL ALLOW FILTERING")
    List<OnboardingSession> findSessionsWithOwnerInfoButNoUser();

    /**
     * Trouve les sessions abandonnées (créées mais jamais mises à jour)
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'IN_PROGRESS' AND owner_info_data IS NULL AND created_at < ?0 ALLOW FILTERING")
    List<OnboardingSession> findAbandonedSessions(LocalDateTime cutoffTime);

    /**
     * Trouve les sessions en cours d'une étape spécifique
     */
    @Query("SELECT * FROM onboarding_sessions WHERE current_step = ?0 AND status = 'IN_PROGRESS' ALLOW FILTERING")
    List<OnboardingSession> findByCurrentStepAndInProgress(String currentStep);

    /**
     * Trouve les sessions créées aujourd'hui
     */
    @Query("SELECT * FROM onboarding_sessions WHERE created_at >= ?0 ALLOW FILTERING")
    List<OnboardingSession> findCreatedSince(LocalDateTime since);

    /**
     * Trouve les sessions qui vont expirer bientôt
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'IN_PROGRESS' AND expires_at <= ?0 AND expires_at > ?1 ALLOW FILTERING")
    List<OnboardingSession> findExpiringSoon(LocalDateTime expirationThreshold, LocalDateTime now);

    /**
     * Compte le nombre total de sessions créées
     */
    @Query("SELECT COUNT(*) FROM onboarding_sessions")
    Long countTotal();

    /**
     * Trouve les sessions avec des données d'organisation mais pas encore terminées
     */
    @Query("SELECT * FROM onboarding_sessions WHERE organization_info_data IS NOT NULL AND status = 'IN_PROGRESS' ALLOW FILTERING")
    List<OnboardingSession> findSessionsWithOrganizationInfo();

    /**
     * Trouve les sessions par période et statut pour les statistiques
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = ?0 AND created_at >= ?1 AND created_at <= ?2 ALLOW FILTERING")
    List<OnboardingSession> findByStatusAndPeriod(OnboardingStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
