package com.innowise.academy.userservice.controller;

import com.innowise.academy.userservice.model.dto.StatusRequest;
import com.innowise.academy.userservice.model.dto.UserDetailsResponse;
import com.innowise.academy.userservice.model.dto.UserRequest;
import com.innowise.academy.userservice.model.dto.UserResponse;
import com.innowise.academy.userservice.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST endpoints for managing users.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user.
     *
     * @param request the user data
     * @return 201 Created with the created user and its location
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Returns one page of users, optionally filtered by name and surname.
     *
     * @param name     part of the first name, optional
     * @param surname  part of the last name, optional
     * @param pageable page number, size and sort order
     * @return 200 OK with the requested page
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(name, surname, pageable));
    }

    /**
     * Returns one user by id, including its payment cards.
     *
     * @param id the user id
     * @return 200 OK with the user and its cards
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * Replaces the data of an existing user.
     *
     * @param id      the user id
     * @param request the new user data
     * @return 200 OK with the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /**
     * Deletes a user together with all of their payment cards.
     *
     * @param id the user id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates or deactivates a user.
     *
     * @param id      the user id
     * @param request the new status
     * @return 204 No Content
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusRequest request) {
        userService.updateStatus(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
