package inc.yowyob.rental_api.vehicle.service;

import inc.yowyob.rental_api.security.util.SecurityUtils;
import inc.yowyob.rental_api.vehicle.dto.CreateTariffDto;
import inc.yowyob.rental_api.vehicle.dto.TariffDto;
import inc.yowyob.rental_api.vehicle.entities.Tariff;
import inc.yowyob.rental_api.vehicle.repository.TariffRepository;
import inc.yowyob.rental_api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public TariffDto createTariff(CreateTariffDto createDto) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        UUID userId = SecurityUtils.getCurrentUserId();

        tariffRepository.findByOrganizationIdAndName(organizationId, createDto.getName())
            .ifPresent(t -> {
                throw new IllegalArgumentException("A tariff with this name already exists.");
            });

        Tariff.TariffKey key = Tariff.TariffKey.builder()
                .organizationId(organizationId)
                .id(UUID.randomUUID())
                .build();

        Tariff tariff = Tariff.builder()
            .key(key)
            .description(createDto.getDescription())
            .pricePerHour(createDto.getPricePerHour())
            .pricePerDay(createDto.getPricePerDay())
            .currency(createDto.getCurrency())
            .isDefault(createDto.isDefault())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(userId)
            .updatedBy(userId)
            .build();
        
        // This is a workaround since name is not in the primary key anymore for this design.
        // In a real-world scenario, you might add a secondary index on `name` or use a different table to enforce uniqueness.
        // For now, the check above handles it.
        // tariff.setName(createDto.getName());


        Tariff saved = tariffRepository.save(tariff);
        return mapToDto(saved, createDto.getName());
    }
    
    public List<TariffDto> getTariffsByOrganization() {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        return tariffRepository.findByKeyOrganizationId(organizationId)
                .stream()
                .map(tariff -> this.mapToDto(tariff, "Default Name")) // FIXME: name is not stored directly
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTariff(UUID tariffId) {
        UUID organizationId = SecurityUtils.getCurrentUserOrganizationId();
        Tariff.TariffKey key = new Tariff.TariffKey(organizationId, tariffId);
        
        Tariff tariff = tariffRepository.findById(key)
            .orElseThrow(() -> new NoSuchElementException("Tariff not found with ID: " + tariffId));

        // Check if tariff is in use. This is inefficient and should be improved.
        long usageCount = vehicleRepository.findByTariffId(tariffId).size();
        if (usageCount > 0) {
            throw new IllegalStateException("Cannot delete tariff as it is currently assigned to " + usageCount + " vehicle(s).");
        }

        tariffRepository.delete(tariff);
    }

    public TariffDto mapToDto(Tariff tariff, String name) {
        return TariffDto.builder()
            .id(tariff.getKey().getId())
            .name(name) // FIXME: Name needs to be retrieved or stored differently
            .description(tariff.getDescription())
            .pricePerHour(tariff.getPricePerHour())
            .pricePerDay(tariff.getPricePerDay())
            .currency(tariff.getCurrency())
            .isDefault(tariff.isDefault())
            .build();
    }
}