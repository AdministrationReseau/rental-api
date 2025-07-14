package inc.yowyob.rental_api.vehicle.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.vehicle.dto.CreateVehicleDto;
import inc.yowyob.rental_api.vehicle.dto.UpdateVehicleDto;
import inc.yowyob.rental_api.vehicle.dto.VehicleDto;
import inc.yowyob.rental_api.vehicle.entities.VehicleStatus;
import inc.yowyob.rental_api.vehicle.service.FileStorageService;
import inc.yowyob.rental_api.vehicle.service.ImageService;
import inc.yowyob.rental_api.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "APIs for vehicles management")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ImageService imageService;
    private final FileStorageService fileStorageService;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    @Operation(summary = "Create a new vehicle")
    public ResponseEntity<ApiResponse<VehicleDto>> createVehicle(@Valid @RequestBody CreateVehicleDto createDto) {
        log.info("POST /vehicles - Creating a new vehicle with registration {}", createDto.getRegistrationNumber());
        try {
            VehicleDto newVehicle = vehicleService.createVehicle(createDto);
            return ApiResponseUtil.created(newVehicle, "Vehicle created successfully");
        } catch (IllegalStateException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponseUtil.conflict(e.getMessage());
        }
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    @Operation(summary = "Get a vehicle by its ID")
    public ResponseEntity<ApiResponse<VehicleDto>> getVehicleById(@PathVariable UUID vehicleId) {
        log.info("GET /vehicles/{} - Fetching vehicle by ID", vehicleId);
        try {
            VehicleDto vehicle = vehicleService.getVehicleById(vehicleId);
            return ApiResponseUtil.success(vehicle, "Vehicle retrieved successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    @Operation(summary = "Get all vehicles of an agency")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehiclesByAgency(@PathVariable UUID agencyId) {
        log.info("GET /vehicles/agency/{} - Fetching vehicles for agency", agencyId);
        try {
            List<VehicleDto> vehicles = vehicleService.getVehiclesByAgency(agencyId);
            return ApiResponseUtil.success(vehicles, "Vehicles retrieved successfully.", vehicles.size());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }

    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
    @Operation(summary = "Update a vehicle")
    public ResponseEntity<ApiResponse<VehicleDto>> updateVehicle(@PathVariable UUID vehicleId, @Valid @RequestBody UpdateVehicleDto updateDto) {
        log.info("PUT /vehicles/{} - Updating vehicle", vehicleId);
        try {
            VehicleDto updatedVehicle = vehicleService.updateVehicle(vehicleId, updateDto);
            return ApiResponseUtil.success(updatedVehicle, "Vehicle updated successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }
    
    @PatchMapping("/{vehicleId}/status")
    @PreAuthorize("hasAuthority('VEHICLE_CHANGE_STATUS')")
    @Operation(summary = "Update vehicle status")
    public ResponseEntity<ApiResponse<VehicleDto>> updateVehicleStatus(@PathVariable UUID vehicleId, @RequestParam VehicleStatus status) {
        log.info("PATCH /vehicles/{}/status - Updating status to {}", vehicleId, status);
        try {
            VehicleDto updatedVehicle = vehicleService.updateVehicleStatus(vehicleId, status);
            return ApiResponseUtil.success(updatedVehicle, "Vehicle status updated successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponseUtil.conflict(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }

    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    @Operation(summary = "Delete a vehicle")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable UUID vehicleId) {
        log.info("DELETE /vehicles/{} - Deleting vehicle", vehicleId);
        try {
            vehicleService.deleteVehicle(vehicleId);
            return ApiResponseUtil.success(null, "Vehicle deleted successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponseUtil.conflict(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }

    @PostMapping("/{vehicleId}/images")
    @PreAuthorize("hasAuthority('VEHICLE_MANAGE_IMAGES')")
    @Operation(summary = "Upload an image for a vehicle")
    public ResponseEntity<ApiResponse<String>> uploadVehicleImage(@PathVariable UUID vehicleId, @RequestParam("file") MultipartFile file) {
        try {
            imageService.attachImageToVehicle(vehicleId, file);
            return ApiResponseUtil.created("Image uploaded successfully.", "Image uploaded and attached to vehicle " + vehicleId);
        } catch (Exception e) {
            log.error("Could not upload image for vehicle {}", vehicleId, e);
            return ApiResponseUtil.error("Failed to upload image.", 500);
        }
    }

    @GetMapping("/images/{fileName:.+}")
    @Operation(summary = "Get a vehicle image file")
    public ResponseEntity<Resource> getVehicleImage(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = "application/octet-stream";
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAuthority('VEHICLE_MANAGE_IMAGES')")
    @Operation(summary = "Delete a vehicle image")
    public ResponseEntity<ApiResponse<Void>> deleteVehicleImage(@PathVariable UUID imageId) {
        try {
            imageService.deleteImage(imageId);
            return ApiResponseUtil.success(null, "Image deleted successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (SecurityException e) {
            return ApiResponseUtil.forbidden(e.getMessage());
        }
    }
}