package inc.yowyob.rental_api.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'affichage des permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDto {

    private String code;
    private String description;
    private String resource;
    private String category; // Calculé à partir du resource
    private Boolean isAssigned; // Indique si la permission est assignée au rôle courant
}
