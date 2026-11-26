package com.innowise.academy.userservice.service.impl;

import com.innowise.academy.userservice.config.CacheConfig;
import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.UserDetailsResponse;
import com.innowise.academy.userservice.model.dto.UserRequest;
import com.innowise.academy.userservice.model.dto.UserResponse;
import com.innowise.academy.userservice.model.entity.UserEntity;
import com.innowise.academy.userservice.model.mapper.UserMapper;
import com.innowise.academy.userservice.repository.UserRepository;
import com.innowise.academy.userservice.repository.specification.UserSpecification;
import com.innowise.academy.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>Read operations run in read-only transactions, write operations in read-write
 * transactions. {@link #getById(Long)} is cached in Redis; every write on a user evicts its
 * cache entry, so the next read reloads it from the database.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        UserEntity user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USERS_CACHE, key = "#id")
    public UserDetailsResponse getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDetailsResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(String name, String surname, Pageable pageable) {
        Specification<UserEntity> spec = UserSpecification.nameContains(name)
                .and(UserSpecification.surnameContains(surname));
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.USERS_CACHE, key = "#id")
    public UserResponse update(Long id, UserRequest request) {
        UserEntity user = findUserOrThrow(id);
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.USERS_CACHE, key = "#id")
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.USERS_CACHE, key = "#id")
    public void updateStatus(Long id, boolean active) {
        if (userRepository.updateActiveStatus(id, active) == 0) {
            throw new UserNotFoundException(id);
        }
    }

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
