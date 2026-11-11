package com.innowise.academy.userservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payment card owned by a {@link User}, mapped to the {@code payment_cards} table.
 *
 * <p>This is the owning side of the user–card relationship: it holds the
 * {@code user_id} foreign key.
 */
@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@NoArgsConstructor
public class PaymentCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 19)
    private String number;

    @Column(nullable = false)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private boolean active = true;
}
