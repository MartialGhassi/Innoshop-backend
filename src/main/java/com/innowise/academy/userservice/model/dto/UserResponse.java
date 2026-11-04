package com.innowise.academy.userservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User data returned by the API.
 *
 * @param id        the user's id
 * @param name      the user's first name
 * @param surname   the user's last name
 * @param birthDate the user's birthdate, may be {@code null}
 * @param email     the user's email address
 * @param active    whether the user is active
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record UserResponse(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
