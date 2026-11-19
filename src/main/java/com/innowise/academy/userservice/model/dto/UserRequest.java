package com.innowise.academy.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data sent by the client to create or update a user.
 *
 * @param name      the user's first name
 * @param surname   the user's last name
 * @param birthDate the user's birthdate, optional, must be in the past
 * @param email     the user's unique email address
 */
public record UserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String surname,
        @Past LocalDate birthDate,
        @NotBlank @Email String email
) {
}
