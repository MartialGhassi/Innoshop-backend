package com.innowise.academy.userservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed user data returned by the API, including the user's payment cards.
 *
 * @param id        the user's id
 * @param name      the user's first name
 * @param surname   the user's last name
 * @param birthDate the user's birthdate, may be {@code null}
 * @param email     the user's email address
 * @param active    whether the user is active
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 * @param cards     the user's payment cards, empty if the user has none
 */
public record UserDetailsResponse(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PaymentCardResponse> cards
) {
}
