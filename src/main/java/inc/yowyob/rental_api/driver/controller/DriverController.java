package inc.yowyob.rental_api.driver.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.driver.dto.CreateDriverDto;
import inc.yowyob.rental_api.driver.dto.DriverDto;
import inc.yowyob.rental_api.driver.dto.UpdateDriverDto;
import inc.yowyob.rental_api.driver.service.DriverService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "CRUD operations for drivers")
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Create a new driver")
    
    // @ApiResponses(value = {
            // @OpenApiResponse(responseCode = "201", description = "Driver created successfully"),
            // @OpenApiResponse(responseCode = "400", description = "Invalid input data"),
            // @OpenApiResponse(responseCode = "403", description = "Forbidden - Lacking permissions")
    // })

    @PostMapping
    // @PreAuthorize("hasAuthority('DRIVER_WRITE')") // SÉCURISATION
    public ResponseEntity<ApiResponse<DriverDto>> createDriver(
        @Valid @RequestBody CreateDriverDto createDriverDto
) {
    UUID currentUserId = SecurityUtils.getCurrentUserId(); // Important pour l'audit
    log.info("POST /drivers - Creating driver for user {}", createDriverDto.getUserId());
    DriverDto created = driverService.createDriver(createDriverDto, currentUserId);
    return ApiResponseUtil.created(created, "Driver created successfully");
}
   
    // CORRECTION : Implémentation du corps de la méthode qui était manquante
    @Operation(summary = "Get all drivers (paginated - for super admin)")
    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')") // Uniquement pour les super admins pour éviter de charger tous les chauffeurs
    public ResponseEntity<ApiResponse<Slice<DriverDto>>> getAllDrivers(Pageable pageable) {
        log.info("GET /drivers - Retrieving all drivers (paginated)");
        Slice<DriverDto> driversSlice = driverService.getAllDrivers(pageable);
        return ApiResponseUtil.success(driversSlice, "All drivers retrieved successfully");
    }
    
    @Operation(summary = "Get all drivers for a specific organization")
     /**
     * Récupère la liste paginée des chauffeurs pour une organisation donnée.
     * L'ID de l'organisation est passé en paramètre de requête.
     * La pagination et le tri sont gérés via l'objet Pageable.
     * Exemple d'appel : /api/v1/drivers?organizationId=...&page=0&size=10&sort=hireDate,desc
     * @param organizationId ID de l'organisation pour laquelle lister les chauffeurs.
     * @param pageable Objet de pagination fourni par Spring.
     * @return Une réponse avec une Page de DTOs de chauffeurs.
     */
     @GetMapping("/organization/{organizationId}")
    // @PreAuthorize("hasAuthority('DRIVER_READ')")
    public ResponseEntity<ApiResponse<Slice<DriverDto>>> getAllDriversByOrganization(
            @RequestParam UUID organizationId,
            Pageable pageable) {
        log.info("Requête GET pour lister les chauffeurs de l'organisation {} avec la pagination {}", organizationId, pageable);
        
        Slice<DriverDto> driversSlice = driverService.getAllDriversByOrganization(organizationId, pageable);
     
        return ApiResponseUtil.success(driversSlice, "Drivers retrieved successfully.");
    }

    @Operation(summary = "Get a driver by ID")
     /**
     * Récupère un chauffeur spécifique par son ID.
     * @param id L'ID du chauffeur à récupérer.
     * @return Une réponse avec le DTO du chauffeur trouvé.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverDto>> getDriverById(@PathVariable UUID id) {
        log.info("Requête GET pour le chauffeur {}", id);
        
        DriverDto driver = driverService.getDriverById(id);
        return ApiResponseUtil.success(driver, "Driver retrieved successfully.");
    }

    @Operation(summary = "Get a driver by agency")
     /**
     * Récupère la liste paginée des chauffeurs pour une AGENCE spécifique.
     *
     * @param agencyId ID de l'agence pour laquelle lister les chauffeurs.
     * @param pageable Objet de pagination fourni par Spring.
     * @return Une réponse avec une Page de DTOs de chauffeurs.
     */
    @GetMapping("/by-agency/{agencyId}")
    // @PreAuthorize("hasAuthority('DRIVER_READ')") // Pensez à sécuriser l'endpoint
    public ResponseEntity<ApiResponse<Slice<DriverDto>>> getAllDriversByAgency(
            @PathVariable UUID agencyId,
            Pageable pageable) {
        log.info("Requête GET pour lister les chauffeurs de l'agence {}", agencyId);
        Slice<DriverDto> driversSlice = driverService.getAllDriversByAgency(agencyId, pageable);
        return ApiResponseUtil.success(driversSlice, "Drivers retrieved successfully.");
    }

    

    @Operation(summary = "Update a driver")
    /**
     * Met à jour les informations d'un chauffeur existant.
     * La mise à jour est partielle : seuls les champs fournis dans le DTO sont modifiés.
     * @param driverId L'ID du chauffeur à mettre à jour.
     * @param updateDriverDto DTO contenant les champs à modifier.
     * @return Une réponse avec le DTO du chauffeur mis à jour.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverDto>> updateDriver(
            @PathVariable("id") UUID driverId,
            @Valid @RequestBody UpdateDriverDto updateDriverDto) {
        // En conditions réelles, l'ID de l'utilisateur viendrait du contexte de sécurité.
        // UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID currentUserId = UUID.randomUUID(); // Placeholder pour le développement
        
        log.info("Requête PUT pour mettre à jour le chauffeur {} par l'utilisateur {}", driverId, currentUserId);
        
        DriverDto updatedDriver = driverService.updateDriver(driverId, updateDriverDto, currentUserId);
        return ApiResponseUtil.success(updatedDriver, "Driver updated successfully.");
    }

    @Operation(summary = "Delete a driver")
    /**
     * Supprime un chauffeur de la base de données.
     * @param id L'ID du chauffeur à supprimer.
     * @return Une réponse vide avec un statut 204 (No Content) pour indiquer le succès.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        log.info("Requête DELETE pour le chauffeur {}", id);
        
        driverService.deleteDriver(id);
        // Utiliser noContent() est sémantiquement correct pour une suppression réussie.
        return ResponseEntity.noContent().build();
    }
}
