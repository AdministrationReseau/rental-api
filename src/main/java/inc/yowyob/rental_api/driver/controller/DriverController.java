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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "CRUD operations for drivers")
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Create a new driver")
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

    @Operation(summary = "Get all drivers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverDto>>> getAllDrivers() {
        log.info("GET /drivers - Retrieving all drivers");
        List<DriverDto> drivers = driverService.getAllDrivers();
        return ApiResponseUtil.success(drivers, "Drivers retrieved successfully");
    }

    @Operation(summary = "Get a driver by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverDto>> getDriverById(
            @PathVariable UUID id
    ) {
        log.info("GET /drivers/{} - Retrieving driver", id);
        DriverDto driver = driverService.getDriverById(id);
        return ApiResponseUtil.success(driver, "Driver retrieved successfully");
    }

    @Operation(summary = "Update a driver")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverDto>> updateDriver(
        @PathVariable("id") UUID driverId,
        @Valid @RequestBody UpdateDriverDto updateDriverDto
) {
    UUID currentUserId = SecurityUtils.getCurrentUserId(); // Important pour l'audit
    log.info("PUT /drivers/{} - Updating driver", driverId);
    DriverDto updated = driverService.updateDriver(driverId, updateDriverDto, currentUserId);
    return ApiResponseUtil.success(updated, "Driver updated successfully");
}

    @Operation(summary = "Delete a driver")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(
            @PathVariable UUID id
    ) {
        log.info("DELETE /drivers/{} - Deleting driver", id);
        driverService.deleteDriver(id);
        return ApiResponseUtil.success(null, "Driver deleted successfully");
    }
}
