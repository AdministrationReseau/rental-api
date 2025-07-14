package inc.yowyob.rental_api.vehicle.service;

import inc.yowyob.rental_api.security.service.PermissionEvaluationService;
import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.subscription.service.SubscriptionService;
import inc.yowyob.rental_api.vehicle.dto.CreateVehicleDto;
import inc.yowyob.rental_api.vehicle.dto.TariffDto;
import inc.yowyob.rental_api.vehicle.dto.UpdateVehicleDto;
import inc.yowyob.rental_api.vehicle.dto.VehicleDto;
import inc.yowyob.rental_api.vehicle.entities.Image;
import inc.yowyob.rental_api.vehicle.entities.Tariff;
import inc.yowyob.rental_api.vehicle.entities.Vehicle;
import inc.yowyob.rental_api.vehicle.entities.VehicleStatus;
import inc.yowyob.rental_api.vehicle.repository.ImageRepository;
import inc.yowyob.rental_api.vehicle.repository.TariffRepository;
import inc.yowyob.rental_api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final SubscriptionService subscriptionService;
    private final PermissionEvaluationService permissionEvaluationService;
    private final TariffRepository tariffRepository;
    private final TariffService tariffService;
    private final ImageRepository imageRepository; 

    @Transactional
    public VehicleDto createVehicle(CreateVehicleDto createDto) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();
        log.info("User {} is creating a vehicle for organization {}", userId, organizationId);

        if (!permissionEvaluationService.hasAgencyAccess(createDto.getAgencyId())) {
            throw new SecurityException("Access denied to this agency.");
        }

        long currentVehicleCount = vehicleRepository.countByKeyOrganizationId(organizationId);
        if (!subscriptionService.canCreateVehicle(organizationId, (int) currentVehicleCount)) {
            throw new IllegalStateException("Vehicle creation limit reached for your subscription plan.");
        }

        vehicleRepository.findByOrganizationIdAndRegistrationNumber(organizationId, createDto.getRegistrationNumber())
            .ifPresent(v -> {
                throw new IllegalArgumentException("A vehicle with this registration number already exists.");
            });
            
        Tariff.TariffKey tariffKey = new Tariff.TariffKey(organizationId, createDto.getTariffId());
        tariffRepository.findById(tariffKey)
            .orElseThrow(() -> new IllegalArgumentException("Invalid Tariff ID for this organization."));

        Vehicle.VehicleKey vehicleKey = Vehicle.VehicleKey.builder()
                .organizationId(organizationId)
                .agencyId(createDto.getAgencyId())
                .id(UUID.randomUUID())
                .build();

        Vehicle vehicle = Vehicle.builder()
            .key(vehicleKey)
            .tariffId(createDto.getTariffId())
            .registrationNumber(createDto.getRegistrationNumber())
            .brand(createDto.getBrand())
            .model(createDto.getModel())
            .year(createDto.getYear())
            .vehicleType(createDto.getVehicleType())
            .color(createDto.getColor())
            .mileage(createDto.getMileage())
            .fuelType(createDto.getFuelType())
            .transmission(createDto.getTransmission())
            .seats(createDto.getSeats())
            .features(createDto.getFeatures())
            .status(VehicleStatus.AVAILABLE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(userId)
            .updatedBy(userId)
            .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle {} created successfully", savedVehicle.getKey().getId());
        return mapToVehicleDto(savedVehicle);
    }
    
    public VehicleDto getVehicleById(UUID vehicleId) {
        Vehicle vehicle = getVehicleAndCheckAccess(vehicleId);
        return mapToVehicleDto(vehicle);
    }

    public List<VehicleDto> getVehiclesByAgency(UUID agencyId) {
        if (!permissionEvaluationService.hasAgencyAccess(agencyId)) {
            throw new SecurityException("Access denied to this agency.");
        }
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        List<Vehicle> vehicles = vehicleRepository.findByKeyOrganizationIdAndKeyAgencyId(organizationId, agencyId);
        return mapToVehicleDtos(vehicles);
    }

    @Transactional
    public VehicleDto updateVehicle(UUID vehicleId, UpdateVehicleDto updateDto) {
        Vehicle vehicle = getVehicleAndCheckAccess(vehicleId);
        UUID userId = SecurityUtils.getCurrentUserId();
        log.info("User {} is updating vehicle {}", userId, vehicleId);
        
        if (updateDto.getTariffId() != null) {
             Tariff.TariffKey tariffKey = new Tariff.TariffKey(vehicle.getKey().getOrganizationId(), updateDto.getTariffId());
             tariffRepository.findById(tariffKey)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Tariff ID for this organization."));
            vehicle.setTariffId(updateDto.getTariffId());
        }

        if (updateDto.getRegistrationNumber() != null) vehicle.setRegistrationNumber(updateDto.getRegistrationNumber());
        if (updateDto.getBrand() != null) vehicle.setBrand(updateDto.getBrand());
        if (updateDto.getModel() != null) vehicle.setModel(updateDto.getModel());
        if (updateDto.getYear() != null) vehicle.setYear(updateDto.getYear());
        if (updateDto.getColor() != null) vehicle.setColor(updateDto.getColor());

        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicle.setUpdatedBy(userId);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToVehicleDto(updatedVehicle);
    }

    @Transactional
    public VehicleDto updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus) {
        Vehicle vehicle = getVehicleAndCheckAccess(vehicleId);
        UUID userId = SecurityUtils.getCurrentUserId();
        log.info("User {} is updating status of vehicle {} to {}", userId, vehicleId, newStatus);
        
        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new IllegalStateException("Cannot change status of a rented vehicle.");
        }
        
        vehicle.setStatus(newStatus);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicle.setUpdatedBy(userId);
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToVehicleDto(updatedVehicle);
    }

    @Transactional
    public void deleteVehicle(UUID vehicleId) {
        Vehicle vehicle = getVehicleAndCheckAccess(vehicleId);
        UUID userId = SecurityUtils.getCurrentUserId();
        log.warn("User {} is deleting vehicle {}", userId, vehicleId);
        
        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new IllegalStateException("Cannot delete a vehicle that is currently rented.");
        }

        vehicleRepository.delete(vehicle);
    }
    
    private Vehicle getVehicleAndCheckAccess(UUID vehicleId) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        // This is inefficient but necessary with the current key structure for a generic lookup.
        return vehicleRepository.findByKeyOrganizationId(organizationId).stream()
                .filter(v -> v.getKey().getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with ID: " + vehicleId));
    }
    
    private VehicleDto mapToVehicleDto(Vehicle vehicle) {
        Tariff.TariffKey tariffKey = new Tariff.TariffKey(vehicle.getKey().getOrganizationId(), vehicle.getTariffId());
        TariffDto tariffDto = tariffRepository.findById(tariffKey)
                .map(tariff -> tariffService.mapToDto(tariff, "Default Name")) // FIXME: tariff name retrieval
                .orElse(null);
        
        List<String> imageUrls = new ArrayList<>();
        if (vehicle.getImageIds() != null && !vehicle.getImageIds().isEmpty()) {
            imageUrls = imageRepository.findAllById(vehicle.getImageIds()).stream()
                    .map(image -> "/api/v1/vehicles/images/" + image.getFileName())
                    .collect(Collectors.toList());
        }

        return buildVehicleDto(vehicle, tariffDto, imageUrls);
    }
    
    private List<VehicleDto> mapToVehicleDtos(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Tariff.TariffKey> tariffKeys = vehicles.stream()
                .map(v -> new Tariff.TariffKey(v.getKey().getOrganizationId(), v.getTariffId()))
                .collect(Collectors.toSet());

        Set<UUID> imageIds = vehicles.stream()
            .map(Vehicle::getImageIds)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        Map<UUID, TariffDto> tariffMap = tariffRepository.findAllById(tariffKeys).stream()
            .map(tariff -> tariffService.mapToDto(tariff, "Default Name")) // FIXME
            .collect(Collectors.toMap(TariffDto::getId, Function.identity()));

        Map<UUID, String> imageUrlMap = imageRepository.findAllById(imageIds).stream()
            .collect(Collectors.toMap(Image::getId, image -> "/api/v1/vehicles/images/" + image.getFileName()));

        return vehicles.stream().map(vehicle -> {
            TariffDto tariffDto = tariffMap.get(vehicle.getTariffId());
            List<String> imageUrls = (vehicle.getImageIds() != null)
                ? vehicle.getImageIds().stream().map(imageUrlMap::get).filter(Objects::nonNull).collect(Collectors.toList())
                : Collections.emptyList();
            return buildVehicleDto(vehicle, tariffDto, imageUrls);
        }).collect(Collectors.toList());
    }

    private VehicleDto buildVehicleDto(Vehicle vehicle, TariffDto tariff, List<String> imageUrls) {
        return VehicleDto.builder()
            .id(vehicle.getKey().getId())
            .organizationId(vehicle.getKey().getOrganizationId())
            .agencyId(vehicle.getKey().getAgencyId())
            .registrationNumber(vehicle.getRegistrationNumber())
            .brand(vehicle.getBrand())
            .model(vehicle.getModel())
            .year(vehicle.getYear())
            .color(vehicle.getColor())
            .mileage(vehicle.getMileage())
            .status(vehicle.getStatus().getDescription())
            .vehicleType(vehicle.getVehicleType())
            .fuelType(vehicle.getFuelType())
            .transmission(vehicle.getTransmission())
            .seats(vehicle.getSeats())
            .features(vehicle.getFeatures())
            .tariff(tariff)
            .imageUrls(imageUrls)
            .createdAt(vehicle.getCreatedAt())
            .updatedAt(vehicle.getUpdatedAt())
            .build();
    }
}