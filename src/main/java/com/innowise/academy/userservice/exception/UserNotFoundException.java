package com.innowise.academy.userservice.exception;

/**
 * Thrown when no user exists with the requested id.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Creates the exception for the given user id.
     *
     * @param id the id that was not found
     */
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }
}
