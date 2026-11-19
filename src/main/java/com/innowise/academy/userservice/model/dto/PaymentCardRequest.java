package com.innowise.academy.userservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data sent by the client to create or update a payment card.
 *
 * <p>The owner is not part of the body: it comes from the request path.
 *
 * @param number         the unique card number
 * @param holder         the name printed on the card
 * @param expirationDate the expiration date, must be in the future
 */
public record PaymentCardRequest(
        @NotBlank @Size(max = 19) String number,
        @NotBlank @Size(max = 255) String holder,
        @NotNull @Future LocalDate expirationDate) {
}
