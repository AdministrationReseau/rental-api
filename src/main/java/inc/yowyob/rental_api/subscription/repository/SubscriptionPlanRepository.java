package inc.yowyob.rental_api.subscription.repository;

import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends CassandraRepository<SubscriptionPlan, UUID> {

    /**
     * Trouve tous les plans actifs
     */
    @Query("SELECT * FROM subscription_plans WHERE isactive = true ALLOW FILTERING")
    List<SubscriptionPlan> findAllActive();

    /**
     * Trouve un plan par nom
     */
    @Query("SELECT * FROM subscription_plans WHERE name = ?0 ALLOW FILTERING")
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Trouve tous les plans avec une fonctionnalité spécifique
     */
    @Query("SELECT * FROM subscription_plans WHERE has_geofencing = true AND isactive = true ALLOW FILTERING")
    List<SubscriptionPlan> findAllWithGeofencing();

    @Query("SELECT * FROM subscription_plans WHERE has_chat = true AND isactive = true ALLOW FILTERING")
    List<SubscriptionPlan> findAllWithChat();

    @Query("SELECT * FROM subscription_plans WHERE has_advanced_reports = true AND isactive = true ALLOW FILTERING")
    List<SubscriptionPlan> findAllWithAdvancedReports();

    /**
     * Trouve les plans par gamme de prix
     */
    @Query("SELECT * FROM subscription_plans WHERE price >= ?0 AND price <= ?1 AND isactive = true ALLOW FILTERING")
    List<SubscriptionPlan> findByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);

    /**
     * Trouve le plan gratuit
     */
    default Optional<SubscriptionPlan> findTrialPlan() {
        return findByName("GRATUIT");
    }
}
