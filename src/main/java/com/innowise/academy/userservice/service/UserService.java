package com.innowise.academy.userservice.service;

import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.UserRequest;
import com.innowise.academy.userservice.model.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Business operations on users.
 *
 * <p>Works only with DTOs: entities never leave the service layer.
 */
public interface UserService {

    /**
     * Creates a new active user.
     *
     * @param request the data sent by the client
     * @return the created user
     */
    UserResponse create(UserRequest request);

    /**
     * Returns the user with the given id.
     *
     * @param id the user id
     * @return the matching user
     * @throws UserNotFoundException if no user exists with this id
     */
    UserResponse getById(Long id);

    /**
     * Returns one page of users, optionally filtered by name and surname.
     *
     * <p>Filters are partial and case-insensitive; a {@code null} or blank value
     * means "no filter".
     *
     * @param name     the searched name, may be {@code null}
     * @param surname  the searched surname, may be {@code null}
     * @param pageable the page number, size and sort order
     * @return the requested page of users
     */
    Page<UserResponse> getAll(String name, String surname, Pageable pageable);

    /**
     * Replaces the data of an existing user.
     *
     * @param id      the user id
     * @param request the new data sent by the client
     * @return the updated user
     * @throws UserNotFoundException if no user exists with this id
     */
    UserResponse update(Long id, UserRequest request);

    /**
     * Deletes a user together with their payment cards.
     *
     * @param id the user id
     * @throws UserNotFoundException if no user exists with this id
     */
    void delete(Long id);

    /**
     * Activates or deactivates a user.
     *
     * @param id     the user id
     * @param active the new status
     * @throws UserNotFoundException if no user exists with this id
     */
    void updateStatus(Long id, boolean active);
}
