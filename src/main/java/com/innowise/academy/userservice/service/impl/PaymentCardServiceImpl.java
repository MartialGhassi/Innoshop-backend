package com.innowise.academy.userservice.service.impl;

import com.innowise.academy.userservice.config.CacheConfig;
import com.innowise.academy.userservice.exception.CardLimitExceededException;
import com.innowise.academy.userservice.exception.CardNotFoundException;
import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.PaymentCardRequest;
import com.innowise.academy.userservice.model.dto.PaymentCardResponse;
import com.innowise.academy.userservice.model.entity.PaymentCardEntity;
import com.innowise.academy.userservice.model.entity.UserEntity;
import com.innowise.academy.userservice.model.mapper.PaymentCardMapper;
import com.innowise.academy.userservice.repository.PaymentCardRepository;
import com.innowise.academy.userservice.repository.UserRepository;
import com.innowise.academy.userservice.repository.specification.PaymentCardSpecification;
import com.innowise.academy.userservice.service.PaymentCardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link PaymentCardService}.
 *
 * <p>Enforces the "maximum 5 active cards per user" rule both when a card is
 * created and when an inactive card is activated again. The owner's row is locked
 * before every activation, and the card's current state is re-read under that lock,
 * so concurrent requests for the same user are processed one after the other and
 * cannot exceed the limit. Read operations run in
 * read-only transactions, write operations in read-write transactions.
 *
 * <p>The owner's cached details include its cards, so every write on a card evicts the
 * owner's entry from the {@value CacheConfig#USERS_CACHE} cache. The eviction is done
 * through the {@link CacheManager} because the owner's id is only known once the card
 * has been loaded.
 */
@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int MAX_ACTIVE_CARDS = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public PaymentCardResponse create(Long userId, PaymentCardRequest request) {
        UserEntity user = lockUserOrThrow(userId);
        checkActiveCardLimit(userId);
        PaymentCardEntity card = paymentCardMapper.toEntity(request);
        card.setUser(user);
        PaymentCardResponse response =
                paymentCardMapper.toResponse(paymentCardRepository.save(card));
        evictCachedUser(userId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCardResponse> getAllByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return paymentCardRepository.findAllByUserId(userId)
                .stream()
                .map(paymentCardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentCardResponse> getAll(String name, String surname, Pageable pageable) {
        Specification<PaymentCardEntity> spec = PaymentCardSpecification.userNameContains(name)
                .and(PaymentCardSpecification.userSurnameContains(surname));
        return paymentCardRepository.findAll(spec, pageable).map(paymentCardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentCardResponse getById(Long id) {
        return paymentCardMapper.toResponse(findCardOrThrow(id));
    }

    @Override
    @Transactional
    public PaymentCardResponse update(Long id, PaymentCardRequest request) {
        PaymentCardEntity card = findCardOrThrow(id);
        paymentCardMapper.updateEntity(request, card);
        PaymentCardResponse response =
                paymentCardMapper.toResponse(paymentCardRepository.saveAndFlush(card));
        evictCachedUser(response.userId());
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = findCardOrThrow(id).getUser().getId();
        paymentCardRepository.deleteById(id);
        evictCachedUser(userId);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active) {
        Long userId = findCardOrThrow(id).getUser().getId();
        if (active) {
            lockUserOrThrow(userId);
            if (!paymentCardRepository.existsByIdAndActiveTrue(id)) {
                checkActiveCardLimit(userId);
            }
        }
        if (paymentCardRepository.updateActiveStatus(id, active) == 0) {
            throw new CardNotFoundException(id);
        }
        evictCachedUser(userId);
    }

    private PaymentCardEntity findCardOrThrow(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    private UserEntity lockUserOrThrow(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void evictCachedUser(Long userId) {
        Cache usersCache = cacheManager.getCache(CacheConfig.USERS_CACHE);
        if (usersCache != null) {
            usersCache.evict(userId);
        }
    }

    private void checkActiveCardLimit(Long userId) {
        if (paymentCardRepository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_CARDS) {
            throw new CardLimitExceededException(userId);
        }
    }
}
