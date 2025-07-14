package inc.yowyob.rental_api.vehicle.service;

import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.vehicle.entities.Image;
import inc.yowyob.rental_api.vehicle.entities.Vehicle;
import inc.yowyob.rental_api.vehicle.repository.ImageRepository;
import inc.yowyob.rental_api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final FileStorageService fileStorageService;
    private final ImageRepository imageRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public Image attachImageToVehicle(UUID vehicleId, MultipartFile file) {
        // 1. Get vehicle and check access
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Vehicle vehicle = vehicleRepository.findByKeyOrganizationId(organizationId).stream()
                .filter(v -> v.getKey().getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with ID: " + vehicleId));

        // 2. Store the physical file
        String fileName = fileStorageService.storeFile(file, organizationId, "vehicles");

        // 3. Create image metadata entity
        Image image = Image.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .resourceId(vehicleId)
                .resourceType("VEHICLE")
                .fileName(fileName)
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .size(file.getSize())
                .createdBy(SecurityUtils.getCurrentUserId())
                .createdAt(LocalDateTime.now())
                .build();
        Image savedImage = imageRepository.save(image);
        
        // 4. Update the vehicle's image set
        if (vehicle.getImageIds() == null) {
            vehicle.setImageIds(new HashSet<>());
        }
        vehicle.getImageIds().add(savedImage.getId());
        vehicleRepository.save(vehicle);
        
        return savedImage;
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        // 1. Get image metadata and check access
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("Image not found with ID: " + imageId));
        if (!image.getOrganizationId().equals(SecurityUtils.getCurrentUserOrganizationId())) {
            throw new SecurityException("Access denied to this image.");
        }
        
        // 2. Remove link from the vehicle
        if ("VEHICLE".equals(image.getResourceType())) {
            UUID organizationId = image.getOrganizationId();
            vehicleRepository.findByKeyOrganizationId(organizationId).stream()
                .filter(v -> v.getKey().getId().equals(image.getResourceId()))
                .findFirst().ifPresent(vehicle -> {
                    vehicle.getImageIds().remove(imageId);
                    vehicleRepository.save(vehicle);
            });
        }
        
        // 3. Delete the physical file
        fileStorageService.deleteFile(image.getFileName());
        
        // 4. Delete the metadata
        imageRepository.delete(image);
    }
}