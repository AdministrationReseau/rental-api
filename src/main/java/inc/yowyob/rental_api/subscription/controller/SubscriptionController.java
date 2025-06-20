package inc.yowyob.rental_api.subscription.controller;

import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "APIs de gestion des forfaits d'abonnement")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
        summary = "Récupérer tous les forfaits disponibles",
        description = "Retourne la liste de tous les forfaits d'abonnement actifs disponibles pour les nouvelles organisations"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Liste des forfaits récupérée avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Erreur interne du serveur"
        )
    })
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getAllPlans() {
        log.info("GET /subscription/plans - Fetching all subscription plans");

        try {
            List<SubscriptionPlan> plans = subscriptionService.getAllActivePlans();
            log.info("Successfully fetched {} subscription plans", plans.size());

            return ApiResponseUtil.success(
                plans,
                "Forfaits d'abonnement récupérés avec succès",
                plans.size()
            );
        } catch (Exception e) {
            log.error("Error fetching subscription plans", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des forfaits d'abonnement",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer un forfait par ID",
        description = "Retourne les détails d'un forfait d'abonnement spécifique"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Forfait trouvé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Forfait non trouvé"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID de forfait invalide"
        )
    })
    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlan>> getPlanById(
        @Parameter(description = "ID du forfait d'abonnement")
        @PathVariable UUID planId
    ) {
        log.info("GET /subscription/plans/{} - Fetching subscription plan", planId);

        try {
            return subscriptionService.getPlanById(planId)
                .map(plan -> {
                    log.info("Successfully fetched subscription plan: {}", plan.getName());
                    return ApiResponseUtil.success(
                        plan,
                        "Forfait d'abonnement trouvé avec succès"
                    );
                })
                .orElseGet(() -> {
                    log.warn("Subscription plan not found: {}", planId);
                    return ApiResponseUtil.notFound(
                        "Forfait d'abonnement non trouvé"
                    );
                });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid plan ID: {}", planId, e);
            return ApiResponseUtil.badRequest(
                "ID de forfait invalide"
            );
        } catch (Exception e) {
            log.error("Error fetching subscription plan: {}", planId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération du forfait d'abonnement",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer le forfait d'essai gratuit",
        description = "Retourne les détails du forfait d'essai gratuit de 30 jours"
    )
    @GetMapping("/plans/trial")
    public ResponseEntity<ApiResponse<SubscriptionPlan>> getTrialPlan() {
        log.info("GET /subscription/plans/trial - Fetching trial subscription plan");

        try {
            return subscriptionService.getTrialPlan()
                .map(plan -> {
                    log.info("Successfully fetched trial subscription plan");
                    return ApiResponseUtil.success(
                        plan,
                        "Forfait d'essai récupéré avec succès"
                    );
                })
                .orElseGet(() -> {
                    log.warn("Trial subscription plan not configured");
                    return ApiResponseUtil.notFound(
                        "Forfait d'essai non configuré"
                    );
                });
        } catch (Exception e) {
            log.error("Error fetching trial subscription plan", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération du forfait d'essai",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les forfaits avec géofencing",
        description = "Retourne la liste des forfaits incluant la fonctionnalité de géofencing"
    )
    @GetMapping("/plans/with-geofencing")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlansWithGeofencing() {
        log.info("GET /subscription/plans/with-geofencing - Fetching plans with geofencing");

        try {
            // Pour l'instant, on filtre côté application
            List<SubscriptionPlan> plans = subscriptionService.getAllActivePlans()
                .stream()
                .filter(plan -> Boolean.TRUE.equals(plan.getHasGeofencing()))
                .toList();

            log.info("Successfully fetched {} plans with geofencing", plans.size());

            return ApiResponseUtil.success(
                plans,
                "Forfaits avec géofencing récupérés avec succès",
                plans.size()
            );
        } catch (Exception e) {
            log.error("Error fetching plans with geofencing", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des forfaits avec géofencing",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les forfaits avec chat",
        description = "Retourne la liste des forfaits incluant la fonctionnalité de chat"
    )
    @GetMapping("/plans/with-chat")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlansWithChat() {
        log.info("GET /subscription/plans/with-chat - Fetching plans with chat");

        try {
            List<SubscriptionPlan> plans = subscriptionService.getAllActivePlans()
                .stream()
                .filter(plan -> Boolean.TRUE.equals(plan.getHasChat()))
                .toList();

            log.info("Successfully fetched {} plans with chat", plans.size());

            return ApiResponseUtil.success(
                plans,
                "Forfaits avec chat récupérés avec succès",
                plans.size()
            );
        } catch (Exception e) {
            log.error("Error fetching plans with chat", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des forfaits avec chat",
                500
            );
        }
    }
}
