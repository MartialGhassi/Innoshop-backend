package com.innowise.academy.userservice.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Data sent by the client to activate or deactivate a user or a payment card.
 *
 * @param active the new status, {@code true} to activate, {@code false} to deactivate
 */
public record StatusRequest(
        @NotNull Boolean active
) {
}
