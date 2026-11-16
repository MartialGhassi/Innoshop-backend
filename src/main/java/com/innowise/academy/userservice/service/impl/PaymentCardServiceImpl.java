package com.innowise.academy.userservice.service.impl;

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
import com.innowise.academy.userservice.service.PaymentCardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link PaymentCardService}.
 *
 * <p>Enforces the "maximum 5 active cards per user" rule both when a card is
 * created and when an inactive card is activated again. The owner's row is locked
 * before the check, so concurrent requests for the same user are processed one
 * after the other and cannot exceed the limit. Read operations run in
 * read-only transactions, write operations in read-write transactions.
 */
@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int MAX_ACTIVE_CARDS = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardResponse create(Long userId, PaymentCardRequest request) {
        UserEntity user = lockUserOrThrow(userId);
        checkActiveCardLimit(userId);
        PaymentCardEntity card = paymentCardMapper.toEntity(request);
        card.setUser(user);
        return paymentCardMapper.toResponse(paymentCardRepository.save(card));
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
    public PaymentCardResponse getById(Long id) {
        return paymentCardMapper.toResponse(findCardOrThrow(id));
    }

    @Override
    @Transactional
    public PaymentCardResponse update(Long id, PaymentCardRequest request) {
        PaymentCardEntity card = findCardOrThrow(id);
        paymentCardMapper.updateEntity(request, card);
        return paymentCardMapper.toResponse(paymentCardRepository.saveAndFlush(card));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!paymentCardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        paymentCardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active) {
        if (active) {
            PaymentCardEntity card = findCardOrThrow(id);
            if (!card.isActive()) {
                Long userId = card.getUser().getId();
                lockUserOrThrow(userId);
                checkActiveCardLimit(userId);
            }
        }
        if (paymentCardRepository.updateActiveStatus(id, active) == 0) {
            throw new CardNotFoundException(id);
        }
    }

    private PaymentCardEntity findCardOrThrow(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    private UserEntity lockUserOrThrow(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void checkActiveCardLimit(Long userId) {
        if (paymentCardRepository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_CARDS) {
            throw new CardLimitExceededException(userId);
        }
    }
}
