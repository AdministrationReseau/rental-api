package inc.yowyob.rental_api.agency.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.agency.dto.*;
import inc.yowyob.rental_api.agency.service.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
@Tag(name = "Agency Management", description = "APIs de gestion des agences")
public class AgencyController {

    private final AgencyService agencyService;

    @Operation(
        summary = "Créer une nouvelle agence",
        description = "Crée une nouvelle agence pour une organisation"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Agence créée avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données d'entrée invalides"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permissions insuffisantes"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Une agence avec ce nom existe déjà"
        )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('AGENCY_WRITE')")
    public ResponseEntity<ApiResponse<AgencyDto>> createAgency(
        @Parameter(description = "Informations de la nouvelle agence")
        @Valid @RequestBody CreateAgencyDto createDto
    ) {
        log.info("POST /agencies - Creating new agency: {} for organization: {}",
            createDto.getName(), createDto.getOrganizationId());

        try {
            AgencyDto createdAgency = agencyService.createAgency(createDto);

            log.info("Agency created successfully with ID: {}", createdAgency.getId());

            return ApiResponseUtil.created(
                createdAgency,
                "Agence créée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data for agency creation: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Cannot create agency: {}", e.getMessage());
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied for agency creation: {}", e.getMessage());
            return ApiResponseUtil.forbidden("Accès refusé");
        } catch (Exception e) {
            log.error("Error creating agency", e);
            return ApiResponseUtil.error(
                "Erreur lors de la création de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer toutes les agences",
        description = "Retourne la liste des agences selon les permissions de l'utilisateur"
    )
    @GetMapping
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<List<AgencyDto>>> getAllAgencies(
        @RequestParam(required = false) UUID organizationId,
        @RequestParam(required = false) Boolean activeOnly
    ) {
        log.info("GET /agencies - Fetching agencies for organization: {}, activeOnly: {}",
            organizationId, activeOnly);

        try {
            List<AgencyDto> agencies = agencyService.getAllAgencies(organizationId, activeOnly);

            return ApiResponseUtil.success(
                agencies,
                "Agences récupérées avec succès",
                agencies.size()
            );
        } catch (Exception e) {
            log.error("Error fetching agencies", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des agences",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer une agence par ID",
        description = "Retourne les détails d'une agence spécifique"
    )
    @GetMapping("/{agencyId}")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<AgencyDto>> getAgencyById(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("GET /agencies/{} - Fetching agency details", agencyId);

        try {
            AgencyDto agency = agencyService.getAgencyById(agencyId);

            return ApiResponseUtil.success(
                agency,
                "Agence récupérée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Agency not found: {}", agencyId);
            return ApiResponseUtil.notFound("Agence non trouvée");
        } catch (SecurityException e) {
            log.warn("Access denied to agency: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé à cette agence");
        } catch (Exception e) {
            log.error("Error fetching agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour une agence",
        description = "Met à jour les informations d'une agence"
    )
    @PutMapping("/{agencyId}")
    @PreAuthorize("hasAuthority('AGENCY_UPDATE')")
    public ResponseEntity<ApiResponse<AgencyDto>> updateAgency(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId,
        @Parameter(description = "Nouvelles informations de l'agence")
        @Valid @RequestBody UpdateAgencyDto updateDto
    ) {
        log.info("PUT /agencies/{} - Updating agency", agencyId);

        try {
            AgencyDto updatedAgency = agencyService.updateAgency(agencyId, updateDto);

            return ApiResponseUtil.success(
                updatedAgency,
                "Agence mise à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update data for agency: {}", agencyId);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to update agency: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour modifier cette agence");
        } catch (Exception e) {
            log.error("Error updating agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Supprimer une agence",
        description = "Supprime définitivement une agence"
    )
    @DeleteMapping("/{agencyId}")
    @PreAuthorize("hasAuthority('AGENCY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteAgency(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("DELETE /agencies/{} - Deleting agency", agencyId);

        try {
            agencyService.deleteAgency(agencyId);

            return ApiResponseUtil.success(
                null,
                "Agence supprimée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Agency not found for deletion: {}", agencyId);
            return ApiResponseUtil.notFound("Agence non trouvée");
        } catch (IllegalStateException e) {
            log.warn("Cannot delete agency: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to delete agency: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour supprimer cette agence");
        } catch (Exception e) {
            log.error("Error deleting agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la suppression de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les agences par organisation",
        description = "Retourne toutes les agences d'une organisation spécifique"
    )
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<List<AgencyDto>>> getAgenciesByOrganization(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("GET /agencies/organization/{} - Fetching agencies by organization", organizationId);

        try {
            List<AgencyDto> agencies = agencyService.getAgenciesByOrganizationId(organizationId);

            return ApiResponseUtil.success(
                agencies,
                "Agences de l'organisation récupérées avec succès",
                agencies.size()
            );
        } catch (SecurityException e) {
            log.warn("Access denied to organization agencies: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé aux agences de cette organisation");
        } catch (Exception e) {
            log.error("Error fetching agencies by organization: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des agences",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les agences actives par organisation",
        description = "Retourne les agences actives d'une organisation spécifique"
    )
    @GetMapping("/organization/{organizationId}/active")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<List<AgencyDto>>> getActiveAgenciesByOrganization(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("GET /agencies/organization/{}/active - Fetching active agencies", organizationId);

        try {
            List<AgencyDto> agencies = agencyService.getActiveAgenciesByOrganizationId(organizationId);

            return ApiResponseUtil.success(
                agencies,
                "Agences actives récupérées avec succès",
                agencies.size()
            );
        } catch (SecurityException e) {
            log.warn("Access denied to active agencies: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé aux agences actives");
        } catch (Exception e) {
            log.error("Error fetching active agencies: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des agences actives",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les agences de l'utilisateur connecté",
        description = "Retourne les agences accessibles à l'utilisateur connecté"
    )
    @GetMapping("/current-user")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<List<AgencyDto>>> getCurrentUserAgencies() {
        log.info("GET /agencies/current-user - Fetching current user agencies");

        try {
            List<AgencyDto> agencies = agencyService.getCurrentUserAgencies();

            return ApiResponseUtil.success(
                agencies,
                "Agences de l'utilisateur récupérées avec succès",
                agencies.size()
            );
        } catch (Exception e) {
            log.error("Error fetching current user agencies", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des agences",
                500
            );
        }
    }

    @Operation(
        summary = "Activer une agence",
        description = "Active une agence désactivée"
    )
    @PatchMapping("/{agencyId}/activate")
    @PreAuthorize("hasAuthority('AGENCY_UPDATE')")
    public ResponseEntity<ApiResponse<AgencyDto>> activateAgency(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("PATCH /agencies/{}/activate - Activating agency", agencyId);

        try {
            AgencyDto activatedAgency = agencyService.activateAgency(agencyId);

            return ApiResponseUtil.success(
                activatedAgency,
                "Agence activée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Agency not found for activation: {}", agencyId);
            return ApiResponseUtil.notFound("Agence non trouvée");
        } catch (SecurityException e) {
            log.warn("Access denied to activate agency: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour activer cette agence");
        } catch (Exception e) {
            log.error("Error activating agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'activation de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Désactiver une agence",
        description = "Désactive une agence active"
    )
    @PatchMapping("/{agencyId}/deactivate")
    @PreAuthorize("hasAuthority('AGENCY_UPDATE')")
    public ResponseEntity<ApiResponse<AgencyDto>> deactivateAgency(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("PATCH /agencies/{}/deactivate - Deactivating agency", agencyId);

        try {
            AgencyDto deactivatedAgency = agencyService.deactivateAgency(agencyId);

            return ApiResponseUtil.success(
                deactivatedAgency,
                "Agence désactivée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Agency not found for deactivation: {}", agencyId);
            return ApiResponseUtil.notFound("Agence non trouvée");
        } catch (SecurityException e) {
            log.warn("Access denied to deactivate agency: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour désactiver cette agence");
        } catch (Exception e) {
            log.error("Error deactivating agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la désactivation de l'agence",
                500
            );
        }
    }

    @Operation(
        summary = "Assigner un gestionnaire à une agence",
        description = "Assigne un gestionnaire à une agence"
    )
    @PutMapping("/{agencyId}/manager")
    @PreAuthorize("hasAuthority('AGENCY_UPDATE')")
    public ResponseEntity<ApiResponse<AgencyDto>> assignManager(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId,
        @Parameter(description = "ID du gestionnaire")
        @RequestParam UUID managerId
    ) {
        log.info("PUT /agencies/{}/manager - Assigning manager {} to agency", agencyId, managerId);

        try {
            AgencyDto updatedAgency = agencyService.assignManager(agencyId, managerId);

            return ApiResponseUtil.success(
                updatedAgency,
                "Gestionnaire assigné avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid manager assignment: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to assign manager: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour assigner un gestionnaire");
        } catch (Exception e) {
            log.error("Error assigning manager to agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation du gestionnaire",
                500
            );
        }
    }

    @Operation(
        summary = "Retirer le gestionnaire d'une agence",
        description = "Retire le gestionnaire assigné à une agence"
    )
    @DeleteMapping("/{agencyId}/manager")
    @PreAuthorize("hasAuthority('AGENCY_UPDATE')")
    public ResponseEntity<ApiResponse<AgencyDto>> removeManager(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("DELETE /agencies/{}/manager - Removing manager from agency", agencyId);

        try {
            AgencyDto updatedAgency = agencyService.removeManager(agencyId);

            return ApiResponseUtil.success(
                updatedAgency,
                "Gestionnaire retiré avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Agency not found: {}", agencyId);
            return ApiResponseUtil.notFound("Agence non trouvée");
        } catch (SecurityException e) {
            log.warn("Access denied to remove manager: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour retirer le gestionnaire");
        } catch (Exception e) {
            log.error("Error removing manager from agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors du retrait du gestionnaire",
                500
            );
        }
    }

    @Operation(
        summary = "Assigner du personnel à une agence",
        description = "Assigne un membre du personnel à une agence"
    )
    @PostMapping("/staff")
    @PreAuthorize("hasAuthority('AGENCY_MANAGE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> assignStaffToAgency(
        @Parameter(description = "Informations d'assignation du personnel")
        @Valid @RequestBody AssignStaffToAgencyDto assignDto
    ) {
        log.info("POST /agencies/staff - Assigning staff {} to agency {}",
            assignDto.getUserId(), assignDto.getAgencyId());

        try {
            agencyService.assignStaffToAgency(assignDto);

            return ApiResponseUtil.success(
                null,
                "Personnel assigné à l'agence avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid staff assignment: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to assign staff: {}", e.getMessage());
            return ApiResponseUtil.forbidden("Accès refusé pour assigner du personnel");
        } catch (Exception e) {
            log.error("Error assigning staff to agency", e);
            return ApiResponseUtil.error(
                "Erreur lors de l'assignation du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Retirer du personnel d'une agence",
        description = "Retire un membre du personnel d'une agence"
    )
    @DeleteMapping("/{agencyId}/staff/{userId}")
    @PreAuthorize("hasAuthority('AGENCY_MANAGE_STAFF')")
    public ResponseEntity<ApiResponse<Void>> removeStaffFromAgency(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId,
        @Parameter(description = "ID de l'utilisateur")
        @PathVariable UUID userId
    ) {
        log.info("DELETE /agencies/{}/staff/{} - Removing staff from agency", agencyId, userId);

        try {
            agencyService.removeStaffFromAgency(agencyId, userId);

            return ApiResponseUtil.success(
                null,
                "Personnel retiré de l'agence avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Staff removal failed: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to remove staff: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé pour retirer du personnel");
        } catch (Exception e) {
            log.error("Error removing staff from agency: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors du retrait du personnel",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer les statistiques d'une agence",
        description = "Retourne les statistiques générales d'une agence"
    )
    @GetMapping("/{agencyId}/stats")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<AgencyStatsDto>> getAgencyStats(
        @Parameter(description = "ID de l'agence")
        @PathVariable UUID agencyId
    ) {
        log.info("GET /agencies/{}/stats - Fetching agency statistics", agencyId);

        try {
            AgencyStatsDto stats = agencyService.getAgencyStats(agencyId);

            return ApiResponseUtil.success(
                stats,
                "Statistiques de l'agence récupérées avec succès"
            );
        } catch (SecurityException e) {
            log.warn("Access denied to agency stats: {}", agencyId);
            return ApiResponseUtil.forbidden("Accès refusé aux statistiques");
        } catch (Exception e) {
            log.error("Error fetching agency stats: {}", agencyId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération des statistiques",
                500
            );
        }
    }

    @Operation(
        summary = "Rechercher des agences",
        description = "Recherche des agences selon différents critères"
    )
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('AGENCY_READ')")
    public ResponseEntity<ApiResponse<List<AgencyDto>>> searchAgencies(
        @Parameter(description = "ID de l'organisation")
        @RequestParam UUID organizationId,
        @Parameter(description = "Ville")
        @RequestParam(required = false) String city,
        @Parameter(description = "Région")
        @RequestParam(required = false) String region,
        @Parameter(description = "Agences actives uniquement")
        @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        log.info("GET /agencies/search - Searching agencies for organization: {}, city: {}, region: {}",
            organizationId, city, region);

        try {
            List<AgencyDto> agencies = agencyService.searchAgencies(organizationId, city, region, activeOnly);

            return ApiResponseUtil.success(
                agencies,
                "Recherche d'agences terminée avec succès",
                agencies.size()
            );
        } catch (SecurityException e) {
            log.warn("Access denied to search agencies: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé pour rechercher les agences");
        } catch (Exception e) {
            log.error("Error searching agencies", e);
            return ApiResponseUtil.error(
                "Erreur lors de la recherche d'agences",
                500
            );
        }
    }
}
