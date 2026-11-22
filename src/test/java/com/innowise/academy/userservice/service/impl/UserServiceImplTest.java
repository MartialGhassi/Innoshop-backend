package com.innowise.academy.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.UserDetailsResponse;
import com.innowise.academy.userservice.model.dto.UserRequest;
import com.innowise.academy.userservice.model.dto.UserResponse;
import com.innowise.academy.userservice.model.entity.UserEntity;
import com.innowise.academy.userservice.model.mapper.UserMapper;
import com.innowise.academy.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Unit tests for {@link UserServiceImpl}, with the repository and the mapper mocked.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest request;
    private UserEntity user;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        request = new UserRequest("Anna", "Smith", LocalDate.of(1995, 5, 10),
                "anna.smith@example.com");
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Anna");
        user.setSurname("Smith");
        user.setEmail("anna.smith@example.com");
        response = new UserResponse(USER_ID, "Anna", "Smith", LocalDate.of(1995, 5, 10),
                "anna.smith@example.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createSavesUserAndReturnsResponse() {
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void getByIdReturnsUserWithCards() {
        UserDetailsResponse details = new UserDetailsResponse(USER_ID, "Anna", "Smith",
                LocalDate.of(1995, 5, 10), "anna.smith@example.com", true,
                LocalDateTime.now(), LocalDateTime.now(), List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toDetailsResponse(user)).thenReturn(details);

        UserDetailsResponse result = userService.getById(USER_ID);

        assertThat(result).isEqualTo(details);
    }

    @Test
    void getByIdThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 1 not found");
    }

    @Test
    void getAllReturnsPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(
                ArgumentMatchers.<Specification<UserEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toResponse(user)).thenReturn(response);

        Page<UserResponse> result = userService.getAll("An", null, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateCopiesRequestAndReturnsResponse() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.update(USER_ID, request);

        assertThat(result).isEqualTo(response);
        verify(userMapper).updateEntity(request, user);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void updateThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(USER_ID, request))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteRemovesExistingUser() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        userService.delete(USER_ID);

        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void deleteThrowsWhenUserDoesNotExist() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateStatusUpdatesExistingUser() {
        when(userRepository.updateActiveStatus(USER_ID, false)).thenReturn(1);

        userService.updateStatus(USER_ID, false);

        verify(userRepository).updateActiveStatus(USER_ID, false);
    }

    @Test
    void updateStatusThrowsWhenUserDoesNotExist() {
        when(userRepository.updateActiveStatus(USER_ID, true)).thenReturn(0);

        assertThatThrownBy(() -> userService.updateStatus(USER_ID, true))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 1 not found");
    }
}
