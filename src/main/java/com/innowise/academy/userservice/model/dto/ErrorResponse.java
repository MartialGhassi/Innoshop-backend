package com.innowise.academy.userservice.model.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform error body returned by the API for every failed request.
 *
 * @param timestamp   when the error occurred
 * @param status      the HTTP status code
 * @param error       the HTTP status reason phrase
 * @param message     a human-readable description of the problem
 * @param path        the request path that caused the error
 * @param fieldErrors the invalid fields and their messages, empty if not a validation error
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
