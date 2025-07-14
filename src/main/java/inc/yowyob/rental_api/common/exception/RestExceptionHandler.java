package inc.yowyob.rental_api.common.exception;

import inc.yowyob.rental_api.common.response.ApiResponse;
import inc.yowyob.rental_api.common.response.ApiResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<ApiResponse<Object>> handleNoSuchElementException(NoSuchElementException ex) {
        return ApiResponseUtil.error(ex.getMessage(), 404);
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        return ApiResponseUtil.error(ex.getMessage(), 409);
    }
}