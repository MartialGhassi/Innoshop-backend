package com.innowise.academy.userservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment card data returned by the API.
 *
 * @param id             the card's id
 * @param userId         the owner's id
 * @param number         the card number
 * @param holder         the name printed on the card
 * @param expirationDate the expiration date
 * @param active         whether the card is active
 * @param createdAt      the creation timestamp
 * @param updatedAt      the last update timestamp
 */
public record PaymentCardResponse(
        Long id,
        Long userId,
        String number,
        String holder,
        LocalDate expirationDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
