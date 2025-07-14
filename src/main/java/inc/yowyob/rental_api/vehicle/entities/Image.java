package inc.yowyob.rental_api.vehicle.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("images")
public class Image {

    @Id
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @NotNull
    private UUID organizationId;

    @NotNull
    private UUID resourceId; // ID of the vehicle, user, etc.

    @NotBlank
    private String resourceType; // "VEHICLE", "USER_PROFILE", etc.

    @NotBlank
    private String fileName; // The generated, unique file name on disk

    @NotBlank
    private String originalFileName;

    @NotBlank
    private String fileType; // e.g., "image/jpeg"

    @NotNull
    private Long size; // in bytes

    // Audit fields
    private LocalDateTime createdAt;
    private UUID createdBy;
}