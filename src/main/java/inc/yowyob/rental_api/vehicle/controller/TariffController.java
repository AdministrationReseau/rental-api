package inc.yowyob.rental_api.vehicle.controller;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import inc.yowyob.rental_api.vehicle.dto.CreateTariffDto;
import inc.yowyob.rental_api.vehicle.dto.TariffDto;
import inc.yowyob.rental_api.vehicle.service.TariffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.NoSuchElementException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariff Management", description = "APIs for managing vehicle tariffs")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasAuthority('TARIFF_WRITE')")
    public ResponseEntity<ApiResponse<TariffDto>> createTariff(@Valid @RequestBody CreateTariffDto createDto) {
        try {
            TariffDto newTariff = tariffService.createTariff(createDto);
            return ApiResponseUtil.created(newTariff, "Tariff created successfully.");
        } catch (IllegalArgumentException e) {
            return ApiResponseUtil.conflict(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TARIFF_READ')")
    public ResponseEntity<ApiResponse<List<TariffDto>>> getTariffs() {
        List<TariffDto> tariffs = tariffService.getTariffsByOrganization();
        return ApiResponseUtil.success(tariffs, "Tariffs retrieved successfully.", tariffs.size());
    }
    
    @DeleteMapping("/{tariffId}")
    @PreAuthorize("hasAuthority('TARIFF_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteTariff(@PathVariable UUID tariffId) {
        try {
            tariffService.deleteTariff(tariffId);
            return ApiResponseUtil.success(null, "Tariff deleted successfully.");
        } catch (NoSuchElementException e) {
            return ApiResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponseUtil.conflict(e.getMessage());
        }
    }
}