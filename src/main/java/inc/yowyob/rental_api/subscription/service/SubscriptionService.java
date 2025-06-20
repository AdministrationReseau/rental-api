package inc.yowyob.rental_api.subscription.service;

import inc.yowyob.rental_api.core.enums.SubscriptionStatus;
import inc.yowyob.rental_api.subscription.entities.OrganizationSubscription;
import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.repository.OrganizationSubscriptionRepository;
import inc.yowyob.rental_api.subscription.repository.SubscriptionPlanRepository;
import inc.yowyob.rental_api.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationSubscriptionRepository organizationSubscriptionRepository;
    private final AppProperties appProperties;

    /**
     * Récupère tous les plans d'abonnement actifs
     */
    public List<SubscriptionPlan> getAllActivePlans() {
        log.debug("Fetching all active subscription plans");
        return subscriptionPlanRepository.findAllActive();
    }

    /**
     * Récupère un plan d'abonnement par ID
     */
    public Optional<SubscriptionPlan> getPlanById(UUID planId) {
        log.debug("Fetching subscription plan with ID: {}", planId);
        return subscriptionPlanRepository.findById(planId);
    }

    /**
     * Récupère le plan d'essai gratuit
     */
    public Optional<SubscriptionPlan> getTrialPlan() {
        log.debug("Fetching trial subscription plan");
        return subscriptionPlanRepository.findTrialPlan();
    }

    /**
     * Crée une nouvelle souscription pour une organisation
     */
    public OrganizationSubscription createSubscription(UUID organizationId, UUID planId,
                                                       String paymentMethod, String transactionId,
                                                       BigDecimal amountPaid) {
        log.info("Creating subscription for organization: {} with plan: {}", organizationId, planId);

        // Vérifier qu'il n'y a pas déjà une souscription active
        Optional<OrganizationSubscription> existingSubscription =
            organizationSubscriptionRepository.findActiveByOrganizationId(organizationId);

        if (existingSubscription.isPresent()) {
            throw new IllegalStateException("Organization already has an active subscription");
        }

        // Récupérer le plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));

        // Calculer les dates
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(plan.getDurationDays());

        // Créer la souscription
        OrganizationSubscription subscription = new OrganizationSubscription(
            organizationId, planId, startDate, endDate
        );

        subscription.setPaymentMethod(paymentMethod);
        subscription.setPaymentTransactionId(transactionId);
        subscription.setAmountPaid(amountPaid);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        // Marquer comme essai si c'est le plan gratuit
        if (plan.isTrialPlan()) {
            subscription.setIsTrial(true);
            subscription.setTrialStartDate(startDate);
            subscription.setTrialEndDate(endDate);
        }

        OrganizationSubscription saved = organizationSubscriptionRepository.save(subscription);
        log.info("Subscription created successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Crée une souscription d'essai
     */
    public OrganizationSubscription createTrialSubscription(UUID organizationId) {
        log.info("Creating trial subscription for organization: {}", organizationId);

        SubscriptionPlan trialPlan = getTrialPlan()
            .orElseThrow(() -> new IllegalStateException("Trial plan not configured"));

        return createSubscription(organizationId, trialPlan.getId(), "TRIAL", null, BigDecimal.ZERO);
    }

    /**
     * Récupère la souscription active d'une organisation
     */
    public Optional<OrganizationSubscription> getActiveSubscription(UUID organizationId) {
        log.debug("Fetching active subscription for organization: {}", organizationId);
        return organizationSubscriptionRepository.findActiveByOrganizationId(organizationId);
    }

    /**
     * Vérifie si une organisation a une souscription active
     */
    public boolean hasActiveSubscription(UUID organizationId) {
        return getActiveSubscription(organizationId)
            .map(OrganizationSubscription::isActive)
            .orElse(false);
    }

    /**
     * Vérifie si une fonctionnalité est disponible pour une organisation
     */
    public boolean hasFeature(UUID organizationId, String feature) {
        log.debug("Checking feature '{}' for organization: {}", feature, organizationId);

        Optional<OrganizationSubscription> subscription = getActiveSubscription(organizationId);
        if (subscription.isEmpty() || !subscription.get().isActive()) {
            return false;
        }

        UUID planId = subscription.get().getSubscriptionPlanId();
        Optional<SubscriptionPlan> plan = getPlanById(planId);

        return plan.map(p -> p.hasFeature(feature)).orElse(false);
    }

    /**
     * Vérifie les limites de ressources pour une organisation
     */
    public boolean canCreateAgency(UUID organizationId, int currentCount) {
        return checkResourceLimit(organizationId, "agencies", currentCount);
    }

    public boolean canCreateVehicle(UUID organizationId, int currentCount) {
        return checkResourceLimit(organizationId, "vehicles", currentCount);
    }

    public boolean canCreateDriver(UUID organizationId, int currentCount) {
        return checkResourceLimit(organizationId, "drivers", currentCount);
    }

    private boolean checkResourceLimit(UUID organizationId, String resourceType, int currentCount) {
        Optional<OrganizationSubscription> subscription = getActiveSubscription(organizationId);
        if (subscription.isEmpty() || !subscription.get().isActive()) {
            return false;
        }

        UUID planId = subscription.get().getSubscriptionPlanId();
        Optional<SubscriptionPlan> plan = getPlanById(planId);

        if (plan.isEmpty()) {
            return false;
        }

        SubscriptionPlan subscriptionPlan = plan.get();
        return switch (resourceType) {
            case "agencies" -> currentCount < subscriptionPlan.getMaxAgencies();
            case "vehicles" -> currentCount < subscriptionPlan.getMaxVehicles();
            case "drivers" -> currentCount < subscriptionPlan.getMaxDrivers();
            default -> false;
        };
    }

    /**
     * Renouvelle une souscription
     */
    public OrganizationSubscription renewSubscription(UUID subscriptionId, String paymentMethod,
                                                      String transactionId, BigDecimal amountPaid) {
        log.info("Renewing subscription: {}", subscriptionId);

        OrganizationSubscription subscription = organizationSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        SubscriptionPlan plan = getPlanById(subscription.getSubscriptionPlanId())
            .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));

        // Étendre la période
        subscription.extendSubscription(plan.getDurationDays());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentMethod(paymentMethod);
        subscription.setPaymentTransactionId(transactionId);
        subscription.setAmountPaid(amountPaid);

        OrganizationSubscription renewed = organizationSubscriptionRepository.save(subscription);
        log.info("Subscription renewed successfully until: {}", renewed.getEndDate());

        return renewed;
    }

    /**
     * Annule une souscription
     */
    public void cancelSubscription(UUID subscriptionId) {
        log.info("Cancelling subscription: {}", subscriptionId);

        OrganizationSubscription subscription = organizationSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        subscription.cancel();
        organizationSubscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully");
    }

    /**
     * Trouve les souscriptions qui expirent bientôt
     */
    public List<OrganizationSubscription> findExpiringSoon(int days) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(days);
        return organizationSubscriptionRepository.findExpiringSoon(expirationDate);
    }

    /**
     * Met à jour les souscriptions expirées
     */
    public void updateExpiredSubscriptions() {
        log.info("Updating expired subscriptions");

        List<OrganizationSubscription> expiredSubscriptions =
            organizationSubscriptionRepository.findExpired(LocalDateTime.now());

        for (OrganizationSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            organizationSubscriptionRepository.save(subscription);
        }

        log.info("Updated {} expired subscriptions", expiredSubscriptions.size());
    }
}
