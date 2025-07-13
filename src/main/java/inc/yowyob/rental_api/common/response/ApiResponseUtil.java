package inc.yowyob.rental_api.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utilitaire pour créer des réponses API standardisées
 */
public class ApiResponseUtil {

    /**
     * Crée une réponse de succès (200)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(200)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Crée une réponse de succès avec métadonnées (200)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message, Object metadata) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .metadata(metadata)
            .statusCode(200)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Crée une réponse de création (201)
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .statusCode(201)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crée une réponse de création avec métadonnées (201)
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message, Object metadata) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .metadata(metadata)
            .statusCode(201)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crée une réponse de succès sans contenu (204)
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Crée une réponse de requête incorrecte (400)
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(400)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Crée une réponse non autorisé (401)
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(401)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Crée une réponse interdit (403)
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(403)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Crée une réponse non trouvé (404)
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(404)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Crée une réponse de conflit (409)
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(409)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Crée une réponse d'erreur interne (500)
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus notFound, String statusCode) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(notFound)
            .statusCode(statusCode)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(string).body(response);
    }

    /**
     * Crée une réponse d'erreur interne avec chemin (500)
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, int statusCode, String path) {
        ApiResponse<T> response = ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .statusCode(statusCode)
            .path(path)
            .timestamp(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.status(statusCode).body(response);
    }
}
