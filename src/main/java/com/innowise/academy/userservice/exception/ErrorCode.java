package com.innowise.academy.userservice.exception;

/**
 * Central catalogue of the error message templates used by the service exceptions.
 *
 * <p>Each constant holds a {@link String#format(String, Object...)} template that is
 * filled with the values of the failing request through {@link #format(Object...)}.
 */
public enum ErrorCode {

    USER_NOT_FOUND("User with id %d not found"),
    CARD_NOT_FOUND("Payment card with id %d not found"),
    CARD_LIMIT_EXCEEDED("User with id %d already has the maximum of 5 active cards"),
    EMAIL_ALREADY_EXISTS("A user with this email already exists"),
    CARD_NUMBER_ALREADY_EXISTS("A payment card with this number already exists"),
    DATA_CONFLICT("The request conflicts with existing data"),
    VALIDATION_FAILED("Request validation failed"),
    INVALID_PARAMETER("Parameter '%s' has an invalid value '%s'"),
    ENDPOINT_NOT_FOUND("No endpoint %s %s"),
    MALFORMED_REQUEST("Malformed or missing request body"),
    INTERNAL_ERROR("An unexpected error occurred");

    private final String template;

    ErrorCode(String template) {
        this.template = template;
    }

    /**
     * Builds the error message by filling the template with the given values.
     *
     * @param args the values inserted into the template, in order
     * @return the formatted error message
     */
    public String format(Object... args) {
        return String.format(template, args);
    }
}
