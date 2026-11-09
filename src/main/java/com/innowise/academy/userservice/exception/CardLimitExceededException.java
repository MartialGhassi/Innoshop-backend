package com.innowise.academy.userservice.exception;

/**
 * Thrown when a user already has the maximum number of active payment cards.
 */
public class CardLimitExceededException extends RuntimeException {

    /**
     * Creates the exception for the given user id.
     *
     * @param userId the id of the user who reached the limit
     */
    public CardLimitExceededException(Long userId) {
        super("User with id " + userId + " already has the maximum of 5 active cards");
    }
}
