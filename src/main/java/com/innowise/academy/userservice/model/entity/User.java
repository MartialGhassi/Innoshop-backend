package com.innowise.academy.userservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User of the shop, mapped to the {@code users} table.
 *
 * <p>Owns its {@link PaymentCard}s: saving or deleting a user cascades to the cards,
 * and a card removed from {@link #getCards()} is deleted.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCard> cards = new ArrayList<>();

    /**
     * Adds a card to this user and keeps both sides of the relationship in sync.
     *
     * @param card the card to attach to this user
     */
    public void addCard(PaymentCard card) {
        cards.add(card);
        card.setUser(this);
    }

    /**
     * Removes a card from this user; thanks to {@code orphanRemoval} it is then
     * deleted from the database.
     *
     * @param card the card to detach from this user
     */
    public void removeCard(PaymentCard card) {
        cards.remove(card);
        card.setUser(null);
    }
}
