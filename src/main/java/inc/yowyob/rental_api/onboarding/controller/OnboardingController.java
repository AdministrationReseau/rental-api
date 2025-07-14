package inc.yowyob.rental_api.onboarding.controller;

import inc.yowyob.rental_api.onboarding.dto.*;
import inc.yowyob.rental_api.onboarding.service.OnboardingService;
import inc.yowyob.rental_api.subscription.entities.SubscriptionPlan;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "APIs pour le processus d'inscription d'organisation")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final SubscriptionService subscriptionService;

    @Operation(
        summary = "Récupérer les forfaits disponibles",
        description = "Retourne la liste des forfaits d'abonnement disponibles pour l'inscription"
    )
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getAvailablePlans() {
        log.info("GET /onboarding/plans - Fetching available subscription plans");

        try {
            List<SubscriptionPlan> plans = subscriptionService.getAllActivePlans();
            log.info("Successfully fetched {} subscription plans for onboarding", plans.size());

            return ApiResponseUtil.success(
                plans,
                "Forfaits d'abonnement disponibles récupérés avec succès",
                plans.size()
            );
        } catch (Exception e) {
            log.error("Error fetching subscription plans for onboarding", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des forfaits d'abonnement",
                500
            );
        }
    }

    @Operation(
        summary = "Créer une session d'onboarding",
        description = "Démarre un nouveau processus d'inscription pour un futur propriétaire (sans utilisateur existant)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Session d'onboarding créée avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Erreur interne du serveur"
        )
    })
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<OnboardingSessionDto>> createSession() {
        log.info("POST /onboarding/session - Creating new onboarding session");

        try {
            OnboardingSessionDto session = onboardingService.createOnboardingSession();
            log.info("Successfully created onboarding session: {}", session.getId());

            return ApiResponseUtil.created(
                session,
                "Session d'onboarding créée avec succès"
            );
        } catch (Exception e) {
            log.error("Error creating onboarding session", e);
            return ApiResponseUtil.error(
                "Erreur lors de la création de la session d'onboarding",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer une session d'onboarding",
        description = "Retourne les détails d'une session d'onboarding spécifique"
    )
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<OnboardingSessionDto>> getSession(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId
    ) {
        log.info("GET /onboarding/session/{} - Fetching onboarding session", sessionId);

        try {
            return onboardingService.getOnboardingSession(sessionId)
                .map(session -> {
                    log.info("Successfully fetched onboarding session: {}", sessionId);
                    return ApiResponseUtil.success(
                        session,
                        "Session d'onboarding récupérée avec succès"
                    );
                })
                .orElseGet(() -> {
                    log.warn("Onboarding session not found: {}", sessionId);
                    return ApiResponseUtil.notFound(
                        "Session d'onboarding non trouvée"
                    );
                });
        } catch (Exception e) {
            log.error("Error fetching onboarding session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération de la session d'onboarding",
                500
            );
        }
    }

    @Operation(
        summary = "Sauvegarder les informations du futur propriétaire (Étape 1)",
        description = "Sauvegarde les informations personnelles du futur propriétaire de l'organisation"
    )
    @PutMapping("/session/{sessionId}/owner-info")
    public ResponseEntity<ApiResponse<OnboardingSessionDto>> saveOwnerInfo(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId,
        @Parameter(description = "Informations du futur propriétaire")
        @Valid @RequestBody OwnerInfoDto ownerInfo
    ) {
        log.info("PUT /onboarding/session/{}/owner-info - Saving owner information", sessionId);

        try {
            OnboardingSessionDto updatedSession = onboardingService.saveOwnerInfo(sessionId, ownerInfo);
            log.info("Successfully saved owner info for session: {}", sessionId);

            return ApiResponseUtil.success(
                updatedSession,
                "Informations du propriétaire sauvegardées avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid session ID or data for owner info: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Invalid session state for owner info: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving owner info for session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la sauvegarde des informations du propriétaire",
                500
            );
        }
    }

    @Operation(
        summary = "Sauvegarder les informations de l'organisation (Étape 2)",
        description = "Sauvegarde les informations et politiques de l'organisation"
    )
    @PutMapping("/session/{sessionId}/organization-info")
    public ResponseEntity<ApiResponse<OnboardingSessionDto>> saveOrganizationInfo(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId,
        @Parameter(description = "Informations de l'organisation")
        @Valid @RequestBody OrganizationInfoDto organizationInfo
    ) {
        log.info("PUT /onboarding/session/{}/organization-info - Saving organization information", sessionId);

        try {
            OnboardingSessionDto updatedSession = onboardingService.saveOrganizationInfo(sessionId, organizationInfo);
            log.info("Successfully saved organization info for session: {}", sessionId);

            return ApiResponseUtil.success(
                updatedSession,
                "Informations de l'organisation sauvegardées avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid session ID or data for organization info: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Invalid session state for organization info: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving organization info for session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la sauvegarde des informations de l'organisation",
                500
            );
        }
    }

    @Operation(
        summary = "Finaliser l'onboarding (Étape 3)",
        description = "Termine le processus d'inscription avec création du compte OWNER, de l'organisation et de l'abonnement"
    )
    @PostMapping("/session/{sessionId}/complete")
    public ResponseEntity<ApiResponse<OnboardingCompletedDto>> completeOnboarding(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId,
        @Parameter(description = "Informations de souscription et paiement")
        @Valid @RequestBody SubscriptionInfoDto subscriptionInfo
    ) {
        log.info("POST /onboarding/session/{}/complete - Completing onboarding", sessionId);

        try {
            OnboardingCompletedDto result = onboardingService.completeOnboarding(sessionId, subscriptionInfo);
            log.info("Successfully completed onboarding for session: {}", sessionId);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtil.success(
                    result,
                    "Processus d'inscription terminé avec succès. Compte propriétaire et organisation créés."
                ).getBody());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid session ID or data for completing onboarding: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Invalid session state for completing onboarding: {}", sessionId, e);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error completing onboarding for session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la finalisation du processus d'inscription",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les informations du propriétaire",
        description = "Retourne les informations du propriétaire pour une session donnée"
    )
    @GetMapping("/session/{sessionId}/owner-info")
    public ResponseEntity<ApiResponse<OwnerInfoDto>> getOwnerInfo(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId
    ) {
        log.info("GET /onboarding/session/{}/owner-info - Fetching owner information", sessionId);

        try {
            return onboardingService.getOwnerInfo(sessionId)
                .map(ownerInfo -> {
                    log.info("Successfully fetched owner info for session: {}", sessionId);
                    return ApiResponseUtil.success(
                        ownerInfo,
                        "Informations du propriétaire récupérées avec succès"
                    );
                })
                .orElseGet(() -> {
                    log.warn("Owner info not found for session: {}", sessionId);
                    return ApiResponseUtil.notFound(
                        "Informations du propriétaire non trouvées pour cette session"
                    );
                });
        } catch (Exception e) {
            log.error("Error fetching owner info for session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des informations du propriétaire",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les informations de l'organisation",
        description = "Retourne les informations de l'organisation pour une session donnée"
    )
    @GetMapping("/session/{sessionId}/organization-info")
    public ResponseEntity<ApiResponse<OrganizationInfoDto>> getOrganizationInfo(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId
    ) {
        log.info("GET /onboarding/session/{}/organization-info - Fetching organization information", sessionId);

        try {
            return onboardingService.getOrganizationInfo(sessionId)
                .map(organizationInfo -> {
                    log.info("Successfully fetched organization info for session: {}", sessionId);
                    return ApiResponseUtil.success(
                        organizationInfo,
                        "Informations de l'organisation récupérées avec succès"
                    );
                })
                .orElseGet(() -> {
                    log.warn("Organization info not found for session: {}", sessionId);
                    return ApiResponseUtil.notFound(
                        "Informations de l'organisation non trouvées pour cette session"
                    );
                });
        } catch (Exception e) {
            log.error("Error fetching organization info for session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des informations de l'organisation",
                500
            );
        }
    }

    @Operation(
        summary = "Annuler une session d'onboarding",
        description = "Annule le processus d'inscription en cours"
    )
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> cancelSession(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId
    ) {
        log.info("DELETE /onboarding/session/{} - Cancelling onboarding session", sessionId);

        try {
            onboardingService.cancelOnboardingSession(sessionId);
            log.info("Successfully cancelled onboarding session: {}", sessionId);

            return ApiResponseUtil.success(
                null,
                "Session d'onboarding annulée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid session ID for cancellation: {}", sessionId, e);
            return ApiResponseUtil.notFound("Session d'onboarding non trouvée");
        } catch (Exception e) {
            log.error("Error cancelling onboarding session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'annulation de la session d'onboarding",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les statistiques d'onboarding",
        description = "Retourne les statistiques globales du processus d'inscription"
    )
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OnboardingStatsDto>> getOnboardingStats() {
        log.info("GET /onboarding/stats - Fetching onboarding statistics");

        try {
            OnboardingStatsDto stats = onboardingService.getOnboardingStats();
            log.info("Successfully fetched onboarding statistics");

            return ApiResponseUtil.success(
                stats,
                "Statistiques d'onboarding récupérées avec succès"
            );
        } catch (Exception e) {
            log.error("Error fetching onboarding statistics", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des statistiques d'onboarding",
                500
            );
        }
    }

    @Operation(
        summary = "Étendre la durée d'une session",
        description = "Prolonge la durée de vie d'une session d'onboarding active"
    )
    @PatchMapping("/session/{sessionId}/extend")
    public ResponseEntity<ApiResponse<OnboardingSessionDto>> extendSession(
        @Parameter(description = "ID de la session d'onboarding")
        @PathVariable UUID sessionId,
        @Parameter(description = "Nombre d'heures à ajouter (défaut: 24)")
        @RequestParam(defaultValue = "24") int additionalHours
    ) {
        log.info("PATCH /onboarding/session/{}/extend - Extending session by {} hours", sessionId, additionalHours);

        try {
            // TODO: Implémenter la méthode extendSession dans le service
            return ApiResponseUtil.success(
                null,
                "Fonctionnalité d'extension de session à implémenter"
            );
        } catch (Exception e) {
            log.error("Error extending onboarding session: {}", sessionId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'extension de la session d'onboarding",
                500
            );
        }
    }

    @Operation(
        summary = "Obtenir les sessions actives",
        description = "Retourne toutes les sessions d'onboarding actuellement en cours"
    )
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<List<OnboardingSessionDto>>> getActiveSessions() {
        log.info("GET /onboarding/sessions/active - Fetching active onboarding sessions");

        try {
            // TODO: Implémenter la méthode getActiveSessions dans le service
            return ApiResponseUtil.success(
                List.of(),
                "Sessions actives récupérées avec succès (fonctionnalité à implémenter)"
            );
        } catch (Exception e) {
            log.error("Error fetching active onboarding sessions", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des sessions actives",
                500
            );
        }
    }

    @Operation(
        summary = "Nettoyer les sessions expirées",
        description = "Met à jour le statut des sessions expirées (tâche administrative)"
    )
    @PostMapping("/sessions/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredSessions() {
        log.info("POST /onboarding/sessions/cleanup - Cleaning up expired sessions");

        try {
            onboardingService.updateExpiredSessions();
            log.info("Successfully cleaned up expired onboarding sessions");

            return ApiResponseUtil.success(
                "Nettoyage effectué avec succès",
                "Sessions expirées mises à jour"
            );
        } catch (Exception e) {
            log.error("Error cleaning up expired onboarding sessions", e);
            return ApiResponseUtil.error(
                "Erreur lors du nettoyage des sessions expirées",
                500
            );
        }
    }
}