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
    CARD_LIMIT_EXCEEDED("User with id %d already has the maximum of 5 active cards");

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
