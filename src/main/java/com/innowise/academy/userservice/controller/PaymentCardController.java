package com.innowise.academy.userservice.controller;

import com.innowise.academy.userservice.model.dto.PaymentCardRequest;
import com.innowise.academy.userservice.model.dto.PaymentCardResponse;
import com.innowise.academy.userservice.model.dto.StatusRequest;
import com.innowise.academy.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST endpoints for managing payment cards.
 *
 * <p>Cards are created and listed under their owner ({@code /api/users/{userId}/cards})
 * and read, updated, deleted or (de)activated directly by id ({@code /api/cards/{id}}).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    /**
     * Creates a new payment card for a user.
     *
     * @param userId  the owner's id
     * @param request the card data
     * @return 201 Created with the created card and its location
     */
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<PaymentCardResponse> create(@PathVariable Long userId,
                                                      @Valid @RequestBody PaymentCardRequest request) {
        PaymentCardResponse created = paymentCardService.create(userId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/cards/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Returns all payment cards of a user.
     *
     * @param userId the owner's id
     * @return 200 OK with the user's cards
     */
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponse>> getAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    /**
     * Returns one payment card by id.
     *
     * @param id the card id
     * @return 200 OK with the card
     */
    @GetMapping("/cards/{id}")
    public ResponseEntity<PaymentCardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    /**
     * Replaces the data of an existing payment card.
     *
     * @param id      the card id
     * @param request the new card data
     * @return 200 OK with the updated card
     */
    @PutMapping("/cards/{id}")
    public ResponseEntity<PaymentCardResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody PaymentCardRequest request) {
        return ResponseEntity.ok(paymentCardService.update(id, request));
    }

    /**
     * Deletes a payment card.
     *
     * @param id the card id
     * @return 204 No Content
     */
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates or deactivates a payment card.
     *
     * @param id      the card id
     * @param request the new status
     * @return 204 No Content
     */
    @PatchMapping("/cards/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusRequest request) {
        paymentCardService.updateStatus(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
