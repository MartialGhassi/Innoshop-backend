package com.innowise.academy.userservice.repository;

import com.innowise.academy.userservice.model.entity.PaymentCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link PaymentCard} entities.
 *
 * <p>Paginated filtering is done through {@link JpaSpecificationExecutor} with the
 * specifications from {@code PaymentCardSpecification}.
 */
public interface PaymentCardRepository
        extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    /**
     * Returns all cards of a user (derived query).
     *
     * @param userId the owner's id
     * @return the user's cards, empty if none
     */
    List<PaymentCard> findAllByUserId(Long userId);

    /**
     * Counts the active cards of a user (derived query), used for the
     * "maximum 5 active cards" rule.
     *
     * @param userId the owner's id
     * @return the number of active cards
     */
    long countByUserIdAndActiveTrue(Long userId);

    /**
     * Activates or deactivates a card with a single native SQL update.
     *
     * @param id     the card id
     * @param active the new status
     * @return the number of updated rows (0 if the card does not exist)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);
}
