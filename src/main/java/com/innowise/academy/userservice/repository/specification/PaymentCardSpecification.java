package com.innowise.academy.userservice.repository.specification;

import com.innowise.academy.userservice.model.entity.PaymentCard;
import com.innowise.academy.userservice.model.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable filters for {@link PaymentCard} queries, based on the owner's name and surname.
 *
 * <p>Each filter is case-insensitive and partial ("contains"); a {@code null} or blank
 * value means "no filter", so filters can always be combined with {@code and}.
 */
public final class PaymentCardSpecification {

    private PaymentCardSpecification() {
    }

    /**
     * Filters cards whose owner's name contains the given value.
     *
     * @param name the searched name, may be {@code null} or blank
     * @return the specification
     */
    public static Specification<PaymentCard> userNameContains(String name) {
        return (root, query, cb) -> {
            if (isBlank(name)) {
                return cb.conjunction();
            }
            Join<PaymentCard, User> user = root.join("user");
            return cb.like(cb.lower(user.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Filters cards whose owner's surname contains the given value.
     *
     * @param surname the searched surname, may be {@code null} or blank
     * @return the specification
     */
    public static Specification<PaymentCard> userSurnameContains(String surname) {
        return (root, query, cb) -> {
            if (isBlank(surname)) {
                return cb.conjunction();
            }
            Join<PaymentCard, User> user = root.join("user");
            return cb.like(cb.lower(user.get("surname")), "%" + surname.toLowerCase() + "%");
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
