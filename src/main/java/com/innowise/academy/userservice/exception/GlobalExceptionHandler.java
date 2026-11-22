package com.innowise.academy.userservice.exception;

import com.innowise.academy.userservice.model.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates the exceptions thrown by the controllers into a uniform {@link ErrorResponse}
 * with the matching HTTP status.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String USER_EMAIL_CONSTRAINT = "uk_users_email";
    private static final String CARD_NUMBER_CONSTRAINT = "uk_payment_cards_number";

    /**
     * Handles requests for a user or a payment card that does not exist.
     *
     * @param ex      the not-found exception
     * @param request the current request
     * @return a 404 error response
     */
    @ExceptionHandler({UserNotFoundException.class, CardNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex,
                                                       HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    /**
     * Handles a request to a path that matches no endpoint.
     *
     * @param ex      the no-resource exception
     * @param request the current request
     * @return a 404 error response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                         HttpServletRequest request) {
        String message = ErrorCode.ENDPOINT_NOT_FOUND.format(request.getMethod(),
                request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, message, request, Map.of());
    }

    /**
     * Handles an attempt to exceed the maximum number of active cards of a user.
     *
     * @param ex      the card limit exception
     * @param request the current request
     * @return a 409 error response
     */
    @ExceptionHandler(CardLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCardLimitExceeded(CardLimitExceededException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    /**
     * Handles a violated database constraint, such as a duplicate email or card number.
     *
     * @param ex      the data integrity exception
     * @param request the current request
     * @return a 409 error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage());
        String message;
        if (cause.contains(USER_EMAIL_CONSTRAINT)) {
            message = ErrorCode.EMAIL_ALREADY_EXISTS.format();
        } else if (cause.contains(CARD_NUMBER_CONSTRAINT)) {
            message = ErrorCode.CARD_NUMBER_ALREADY_EXISTS.format();
        } else {
            log.warn("Data integrity violation on {}: {}", request.getRequestURI(), cause);
            message = ErrorCode.DATA_CONFLICT.format();
        }
        return buildResponse(HttpStatus.CONFLICT, message, request, Map.of());
    }

    /**
     * Handles a request body that fails bean validation.
     *
     * @param ex      the validation exception
     * @param request the current request
     * @return a 400 error response listing the invalid fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.format(),
                request, fieldErrors);
    }

    /**
     * Handles a path variable or request parameter with a wrong type, e.g. a non-numeric id.
     *
     * @param ex      the type mismatch exception
     * @param request the current request
     * @return a 400 error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = ErrorCode.INVALID_PARAMETER.format(ex.getName(), ex.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, Map.of());
    }

    /**
     * Handles a missing or malformed JSON request body.
     *
     * @param ex      the unreadable message exception
     * @param request the current request
     * @return a 400 error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST.format(),
                request, Map.of());
    }

    /**
     * Handles every remaining exception. Spring MVC client errors (e.g. 405 unsupported method,
     * 415 unsupported media type) keep their own status; anything else is logged and reported
     * as 500 without exposing internal details.
     *
     * @param ex      the unexpected exception
     * @param request the current request
     * @return the framework's status for Spring MVC errors, otherwise a 500 error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
                                                          HttpServletRequest request) {
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                return buildResponse(status, springError.getBody().getDetail(), request, Map.of());
            }
        }
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.format(),
                request, Map.of());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message,
                                                        HttpServletRequest request,
                                                        Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
