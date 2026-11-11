package com.innowise.academy.userservice.service.impl;

import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.UserRequest;
import com.innowise.academy.userservice.model.dto.UserResponse;
import com.innowise.academy.userservice.model.entity.User;
import com.innowise.academy.userservice.model.mapper.UserMapper;
import com.innowise.academy.userservice.repository.UserRepository;
import com.innowise.academy.userservice.repository.specification.UserSpecification;
import com.innowise.academy.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>Read operations run in read-only transactions; write operations override this
 * with a read-write transaction.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<UserResponse> getAll(String name, String surname, Pageable pageable) {
        Specification<User> spec = UserSpecification.nameContains(name)
                .and(UserSpecification.surnameContains(surname));
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findUserOrThrow(id);
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active) {
        if (userRepository.updateActiveStatus(id, active) == 0) {
            throw new UserNotFoundException(id);
        }
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
