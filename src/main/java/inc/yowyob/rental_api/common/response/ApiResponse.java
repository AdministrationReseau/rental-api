package inc.yowyob.rental_api.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Classe de réponse API standardisée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object metadata;
    private int statusCode;
    private LocalDateTime timestamp;
    private String path;

    // Constructeurs de convenance
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(200)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, Object metadata) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .metadata(metadata)
            .statusCode(200)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, String path) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
