package inc.yowyob.rental_api.subscription.config;

import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Initialise les données de base pour les forfaits d'abonnement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionDataInitializer implements CommandLineRunner {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing subscription plans data...");

        try {
            // Vérifier si des plans existent déjà
            List<SubscriptionPlan> existingPlans = subscriptionPlanRepository.findAllActive();
            if (!existingPlans.isEmpty()) {
                log.info("Subscription plans already exist. Skipping initialization.");
                return;
            }

            createSubscriptionPlans();
            log.info("Subscription plans initialization completed successfully.");

        } catch (Exception e) {
            log.error("Error during subscription plans initialization: {}", e.getMessage(), e);
        }
    }

    private void createSubscriptionPlans() {
        log.info("Creating default subscription plans...");

        try {
            // Plan GRATUIT (Trial)
            SubscriptionPlan trialPlan = createTrialPlan();
            subscriptionPlanRepository.save(trialPlan);
            log.info("Created GRATUIT plan: {}", trialPlan.getId());

            // Plan BASIC
            SubscriptionPlan basicPlan = createBasicPlan();
            subscriptionPlanRepository.save(basicPlan);
            log.info("Created BASIC plan: {}", basicPlan.getId());

            // Plan PREMIUM
            SubscriptionPlan premiumPlan = createPremiumPlan();
            subscriptionPlanRepository.save(premiumPlan);
            log.info("Created PREMIUM plan: {}", premiumPlan.getId());

            // Plan ENTERPRISE
            SubscriptionPlan enterprisePlan = createEnterprisePlan();
            subscriptionPlanRepository.save(enterprisePlan);
            log.info("Created ENTERPRISE plan: {}", enterprisePlan.getId());

        } catch (Exception e) {
            log.error("Error creating subscription plans: {}", e.getMessage(), e);
            throw e;
        }
    }

    private SubscriptionPlan createTrialPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setId(UUID.randomUUID());
        plan.setName("GRATUIT");
        plan.setDescription("Forfait d'essai gratuit de 30 jours pour découvrir notre plateforme");
        plan.setPrice(BigDecimal.ZERO);
        plan.setDurationDays(30);
        plan.setMaxAgencies(1);
        plan.setMaxVehicles(5);
        plan.setMaxDrivers(2);
        plan.setHasGeofencing(false);
        plan.setHasChat(false);
        plan.setHasAdvancedReports(false);
        plan.setHasApiAccess(false);
        plan.setHasPrioritySupport(false);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return plan;
    }

    private SubscriptionPlan createBasicPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setId(UUID.randomUUID());
        plan.setName("BASIC");
        plan.setDescription("Forfait de base idéal pour les petites entreprises de location");
        plan.setPrice(new BigDecimal("29.99"));
        plan.setDurationDays(30); // Mensuel
        plan.setMaxAgencies(3);
        plan.setMaxVehicles(20);
        plan.setMaxDrivers(10);
        plan.setHasGeofencing(false);
        plan.setHasChat(true);
        plan.setHasAdvancedReports(false);
        plan.setHasApiAccess(false);
        plan.setHasPrioritySupport(false);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return plan;
    }

    private SubscriptionPlan createPremiumPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setId(UUID.randomUUID());
        plan.setName("PREMIUM");
        plan.setDescription("Forfait premium avec fonctionnalités avancées pour les entreprises en croissance");
        plan.setPrice(new BigDecimal("79.99"));
        plan.setDurationDays(30); // Mensuel
        plan.setMaxAgencies(10);
        plan.setMaxVehicles(100);
        plan.setMaxDrivers(50);
        plan.setHasGeofencing(true);
        plan.setHasChat(true);
        plan.setHasAdvancedReports(true);
        plan.setHasApiAccess(true);
        plan.setHasPrioritySupport(false);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return plan;
    }

    private SubscriptionPlan createEnterprisePlan() {
        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setId(UUID.randomUUID());
        plan.setName("ENTERPRISE");
        plan.setDescription("Solution complète pour les grandes entreprises avec support dédié");
        plan.setPrice(new BigDecimal("199.99"));
        plan.setDurationDays(30); // Mensuel
        plan.setMaxAgencies(Integer.MAX_VALUE); // Illimité
        plan.setMaxVehicles(Integer.MAX_VALUE); // Illimité
        plan.setMaxDrivers(Integer.MAX_VALUE); // Illimité
        plan.setHasGeofencing(true);
        plan.setHasChat(true);
        plan.setHasAdvancedReports(true);
        plan.setHasApiAccess(true);
        plan.setHasPrioritySupport(true);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return plan;
    }
}
