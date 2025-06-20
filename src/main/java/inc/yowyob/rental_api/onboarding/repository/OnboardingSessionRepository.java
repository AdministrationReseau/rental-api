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
     * Trouve la session active d'un utilisateur
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
     * Trouve les sessions expirées
     */
    @Query("SELECT * FROM onboarding_sessions WHERE expires_at < ?0 AND status = 'IN_PROGRESS' ALLOW FILTERING")
    List<OnboardingSession> findExpiredSessions(LocalDateTime now);

    /**
     * Trouve les sessions créées dans une période
     */
    @Query("SELECT * FROM onboarding_sessions WHERE created_at >= ?0 AND created_at <= ?1 ALLOW FILTERING")
    List<OnboardingSession> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les sessions terminées
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
     * Trouve les sessions en cours depuis plus de X heures
     */
    @Query("SELECT * FROM onboarding_sessions WHERE status = 'IN_PROGRESS' AND created_at < ?0 ALLOW FILTERING")
    List<OnboardingSession> findStaleInProgressSessions(LocalDateTime cutoffTime);
}
