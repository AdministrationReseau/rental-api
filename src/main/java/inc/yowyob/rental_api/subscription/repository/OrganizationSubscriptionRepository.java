package inc.yowyob.rental_api.subscription.repository;

import inc.yowyob.rental_api.core.enums.SubscriptionStatus;
import inc.yowyob.rental_api.subscription.entities.OrganizationSubscription;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationSubscriptionRepository extends CassandraRepository<OrganizationSubscription, UUID> {

    /**
     * Trouve la souscription active d'une organisation
     */
    @Query("SELECT * FROM organization_subscriptions WHERE organization_id = ?0 AND status = 'ACTIVE' ALLOW FILTERING")
    Optional<OrganizationSubscription> findActiveByOrganizationId(UUID organizationId);

    /**
     * Trouve toutes les souscriptions d'une organisation
     */
    @Query("SELECT * FROM organization_subscriptions WHERE organization_id = ?0 ALLOW FILTERING")
    List<OrganizationSubscription> findAllByOrganizationId(UUID organizationId);

    /**
     * Trouve les souscriptions par statut
     */
    @Query("SELECT * FROM organization_subscriptions WHERE status = ?0 ALLOW FILTERING")
    List<OrganizationSubscription> findAllByStatus(SubscriptionStatus status);

    /**
     * Trouve les souscriptions qui expirent bientôt
     */
    @Query("SELECT * FROM organization_subscriptions WHERE end_date <= ?0 AND status = 'ACTIVE' ALLOW FILTERING")
    List<OrganizationSubscription> findExpiringSoon(LocalDateTime expirationDate);

    /**
     * Trouve les souscriptions expirées
     */
    @Query("SELECT * FROM organization_subscriptions WHERE end_date < ?0 AND status = 'ACTIVE' ALLOW FILTERING")
    List<OrganizationSubscription> findExpired(LocalDateTime now);

    /**
     * Trouve les souscriptions d'essai
     */
    @Query("SELECT * FROM organization_subscriptions WHERE is_trial = true ALLOW FILTERING")
    List<OrganizationSubscription> findAllTrials();

    /**
     * Trouve les souscriptions d'essai actives
     */
    @Query("SELECT * FROM organization_subscriptions WHERE is_trial = true AND status = 'ACTIVE' ALLOW FILTERING")
    List<OrganizationSubscription> findActiveTrials();

    /**
     * Trouve les souscriptions par plan
     */
    @Query("SELECT * FROM organization_subscriptions WHERE subscription_plan_id = ?0 ALLOW FILTERING")
    List<OrganizationSubscription> findAllBySubscriptionPlanId(UUID subscriptionPlanId);

    /**
     * Compte les souscriptions actives par plan
     */
    @Query("SELECT COUNT(*) FROM organization_subscriptions WHERE subscription_plan_id = ?0 AND status = 'ACTIVE' ALLOW FILTERING")
    Long countActiveBySubscriptionPlanId(UUID subscriptionPlanId);

    /**
     * Trouve les souscriptions avec renouvellement automatique
     */
    @Query("SELECT * FROM organization_subscriptions WHERE auto_renewal = true AND status = 'ACTIVE' ALLOW FILTERING")
    List<OrganizationSubscription> findAllWithAutoRenewal();
}
