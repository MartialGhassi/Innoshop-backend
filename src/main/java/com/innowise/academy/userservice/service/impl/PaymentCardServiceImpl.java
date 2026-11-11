package com.innowise.academy.userservice.service.impl;

import com.innowise.academy.userservice.exception.CardLimitExceededException;
import com.innowise.academy.userservice.exception.CardNotFoundException;
import com.innowise.academy.userservice.exception.UserNotFoundException;
import com.innowise.academy.userservice.model.dto.PaymentCardRequest;
import com.innowise.academy.userservice.model.dto.PaymentCardResponse;
import com.innowise.academy.userservice.model.entity.PaymentCard;
import com.innowise.academy.userservice.model.entity.User;
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
 * created and when an inactive card is activated again. Read operations run in
 * read-only transactions; write operations override this with a read-write
 * transaction.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int MAX_ACTIVE_CARDS = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardResponse create(Long userId, PaymentCardRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        checkActiveCardLimit(userId);
        PaymentCard card = paymentCardMapper.toEntity(request);
        card.setUser(user);
        return paymentCardMapper.toResponse(paymentCardRepository.save(card));
    }

    @Override
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
    public PaymentCardResponse getById(Long id) {
        return paymentCardMapper.toResponse(findCardOrThrow(id));
    }

    @Override
    @Transactional
    public PaymentCardResponse update(Long id, PaymentCardRequest request) {
        PaymentCard card = findCardOrThrow(id);
        paymentCardMapper.updateEntity(request, card);
        return paymentCardMapper.toResponse(card);
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
            PaymentCard card = findCardOrThrow(id);
            if (!card.isActive()) {
                checkActiveCardLimit(card.getUser().getId());
            }
        }
        if (paymentCardRepository.updateActiveStatus(id, active) == 0) {
            throw new CardNotFoundException(id);
        }
    }

    private PaymentCard findCardOrThrow(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    private void checkActiveCardLimit(Long userId) {
        if (paymentCardRepository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_CARDS) {
            throw new CardLimitExceededException(userId);
        }
    }
}
