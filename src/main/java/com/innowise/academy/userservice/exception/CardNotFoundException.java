package com.innowise.academy.userservice.exception;

/**
 * Thrown when no payment card exists with the requested id.
 */
public class CardNotFoundException extends RuntimeException {

    /**
     * Creates the exception for the given card id.
     *
     * @param id the id that was not found
     */
    public CardNotFoundException(Long id) {
        super("Payment card with id " + id + " not found");
    }
}
