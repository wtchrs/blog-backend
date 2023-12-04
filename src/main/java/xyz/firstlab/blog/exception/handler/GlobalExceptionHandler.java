package xyz.firstlab.blog.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import xyz.firstlab.blog.dto.ErrorResponse;
import xyz.firstlab.blog.exception.DuplicateUsernameException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException exception) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", exception.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatusCode statusCode = exception.getStatusCode();
        String reasonPhrase = HttpStatus.resolve(statusCode.value()).getReasonPhrase();
        String reason = exception.getReason();

        return ResponseEntity
                .status(statusCode)
                .body(new ErrorResponse(reasonPhrase, reason));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateUsernameException(DuplicateUsernameException exception) {
        return new ErrorResponse("CONFLICT", exception.getMessage());
    }

}
