package com.innowise.academy.userservice.service;

import com.innowise.academy.userservice.exception.CardLimitExceededException;
import com.innowise.academy.userservice.exception.CardNotFoundException;
import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.PaymentCardRequest;
import com.innowise.academy.userservice.model.dto.PaymentCardResponse;
import java.util.List;

/**
 * Business operations on payment cards.
 *
 * <p>Works only with DTOs: entities never leave the service layer. A user may own
 * at most 5 active cards at the same time.
 */
public interface PaymentCardService {

    /**
     * Creates a new active card for the given user.
     *
     * @param userId  the owner's id
     * @param request the data sent by the client
     * @return the created card
     * @throws UserNotFoundException      if no user exists with this id
     * @throws CardLimitExceededException if the user already has 5 active cards
     */
    PaymentCardResponse create(Long userId, PaymentCardRequest request);

    /**
     * Returns all cards of the given user.
     *
     * @param userId the owner's id
     * @return the user's cards, empty if none
     * @throws UserNotFoundException if no user exists with this id
     */
    List<PaymentCardResponse> getAllByUserId(Long userId);

    /**
     * Returns the card with the given id.
     *
     * @param id the card id
     * @return the matching card
     * @throws CardNotFoundException if no card exists with this id
     */
    PaymentCardResponse getById(Long id);

    /**
     * Replaces the data of an existing card.
     *
     * @param id      the card id
     * @param request the new data sent by the client
     * @return the updated card
     * @throws CardNotFoundException if no card exists with this id
     */
    PaymentCardResponse update(Long id, PaymentCardRequest request);

    /**
     * Deletes a card.
     *
     * @param id the card id
     * @throws CardNotFoundException if no card exists with this id
     */
    void delete(Long id);

    /**
     * Activates or deactivates a card.
     *
     * <p>Activating a card is refused when its owner already has 5 active cards.
     *
     * @param id     the card id
     * @param active the new status
     * @throws CardNotFoundException      if no card exists with this id
     * @throws CardLimitExceededException if activation would exceed the limit
     */
    void updateStatus(Long id, boolean active);
}
