package inc.yowyob.rental_api.organization.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.organization.dto.*;
import inc.yowyob.rental_api.organization.service.OrganizationService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "APIs de gestion des organisations")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(
        summary = "Récupérer l'organisation de l'utilisateur connecté",
        description = "Retourne l'organisation de l'utilisateur connecté"
    )
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('ORGANIZATION_READ')")
    public ResponseEntity<ApiResponse<OrganizationDto>> getCurrentUserOrganization() {
        log.info("GET /organizations/current - Fetching current user organization");

        try {
            Optional<OrganizationDto> organization = organizationService.getCurrentUserOrganization();

            if (organization.isPresent()) {
                return ApiResponseUtil.success(
                    organization.get(),
                    "Organisation récupérée avec succès"
                );
            } else {
                return ApiResponseUtil.notFound("Aucune organisation trouvée pour cet utilisateur");
            }
        } catch (Exception e) {
            log.error("Error fetching current user organization", e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération de l'organisation",
                500
            );
        }
    }

    @Operation(
        summary = "Récupérer une organisation par ID",
        description = "Retourne les détails d'une organisation spécifique"
    )
    @GetMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('ORGANIZATION_READ')")
    public ResponseEntity<ApiResponse<OrganizationDto>> getOrganizationById(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("GET /organizations/{} - Fetching organization details", organizationId);

        try {
            OrganizationDto organization = organizationService.getOrganizationById(organizationId);

            return ApiResponseUtil.success(
                organization,
                "Organisation récupérée avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Organization not found: {}", organizationId);
            return ApiResponseUtil.notFound("Organisation non trouvée");
        } catch (SecurityException e) {
            log.warn("Access denied to organization: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé à cette organisation");
        } catch (Exception e) {
            log.error("Error fetching organization: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la récupération de l'organisation",
                500
            );
        }
    }

    @Operation(
        summary = "Mettre à jour une organisation",
        description = "Met à jour les informations d'une organisation"
    )
    @PutMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public ResponseEntity<ApiResponse<OrganizationDto>> updateOrganization(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId,
        @Parameter(description = "Nouvelles informations de l'organisation")
        @Valid @RequestBody UpdateOrganizationDto updateDto
    ) {
        log.info("PUT /organizations/{} - Updating organization", organizationId);

        try {
            OrganizationDto updatedOrganization = organizationService.updateOrganization(organizationId, updateDto);

            return ApiResponseUtil.success(
                updatedOrganization,
                "Organisation mise à jour avec succès"
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update data for organization: {}", organizationId);
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (SecurityException e) {
            log.warn("Access denied to update organization: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé pour modifier cette organisation");
        } catch (Exception e) {
            log.error("Error updating organization: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la mise à jour de l'organisation",
                500
            );
        }
    }

    @Operation(
        summary = "Vérifier si l'organisation peut créer une nouvelle agence",
        description = "Vérifie les limites d'abonnement pour la création d'agence"
    )
    @GetMapping("/{organizationId}/can-create-agency")
    @PreAuthorize("hasAuthority('AGENCY_WRITE')")
    public ResponseEntity<ApiResponse<Boolean>> canCreateAgency(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("GET /organizations/{}/can-create-agency - Checking agency creation limit", organizationId);

        try {
            boolean canCreate = organizationService.canCreateAgency(organizationId);

            return ApiResponseUtil.success(
                canCreate,
                canCreate ? "Création d'agence autorisée" : "Limite d'agences atteinte"
            );
        } catch (SecurityException e) {
            log.warn("Access denied to check agency creation: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé");
        } catch (Exception e) {
            log.error("Error checking agency creation limit: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la vérification des limites",
                500
            );
        }
    }

    @Operation(
        summary = "Incrémenter le compteur d'agences",
        description = "Incrémente le nombre d'agences de l'organisation (usage interne)"
    )
    @PostMapping("/{organizationId}/increment-agency-count")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> incrementAgencyCount(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("POST /organizations/{}/increment-agency-count - Incrementing agency count", organizationId);

        try {
            organizationService.incrementAgencyCount(organizationId);

            return ApiResponseUtil.success(
                null,
                "Compteur d'agences incrémenté avec succès"
            );
        } catch (SecurityException e) {
            log.warn("Access denied to increment agency count: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé");
        } catch (Exception e) {
            log.error("Error incrementing agency count: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de l'incrémentation du compteur",
                500
            );
        }
    }

    @Operation(
        summary = "Décrémenter le compteur d'agences",
        description = "Décrémente le nombre d'agences de l'organisation (usage interne)"
    )
    @PostMapping("/{organizationId}/decrement-agency-count")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> decrementAgencyCount(
        @Parameter(description = "ID de l'organisation")
        @PathVariable UUID organizationId
    ) {
        log.info("POST /organizations/{}/decrement-agency-count - Decrementing agency count", organizationId);

        try {
            organizationService.decrementAgencyCount(organizationId);

            return ApiResponseUtil.success(
                null,
                "Compteur d'agences décrémenté avec succès"
            );
        } catch (SecurityException e) {
            log.warn("Access denied to decrement agency count: {}", organizationId);
            return ApiResponseUtil.forbidden("Accès refusé");
        } catch (Exception e) {
            log.error("Error decrementing agency count: {}", organizationId, e);
            return ApiResponseUtil.error(
                "Erreur lors de la décrémentation du compteur",
                500
            );
        }
    }
}
