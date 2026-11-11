package com.innowise.academy.userservice.repository.specification;

import com.innowise.academy.userservice.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable filters for {@link User} queries.
 *
 * <p>Each filter is case-insensitive and partial ("contains"); a {@code null} or blank
 * value means "no filter", so filters can always be combined with {@code and}.
 */
public final class UserSpecification {

    private UserSpecification() {
    }

    /**
     * Filters users whose name contains the given value.
     *
     * @param name the searched name, may be {@code null} or blank
     * @return the specification
     */
    public static Specification<User> nameContains(String name) {
        return (root, query, cb) -> isBlank(name)
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    /**
     * Filters users whose surname contains the given value.
     *
     * @param surname the searched surname, may be {@code null} or blank
     * @return the specification
     */
    public static Specification<User> surnameContains(String surname) {
        return (root, query, cb) -> isBlank(surname)
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
