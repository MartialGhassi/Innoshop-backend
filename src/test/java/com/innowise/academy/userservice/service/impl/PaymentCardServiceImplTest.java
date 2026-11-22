package com.innowise.academy.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Unit tests for {@link PaymentCardServiceImpl}, with the repositories, the mapper and the
 * cache manager mocked.
 */
@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long CARD_ID = 10L;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache usersCache;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private PaymentCardRequest request;
    private UserEntity user;
    private PaymentCardEntity card;
    private PaymentCardResponse response;

    @BeforeEach
    void setUp() {
        request = new PaymentCardRequest("4000000000000001", "ANNA SMITH",
                LocalDate.of(2030, 1, 31));
        user = new UserEntity();
        user.setId(USER_ID);
        card = new PaymentCardEntity();
        card.setId(CARD_ID);
        card.setUser(user);
        response = new PaymentCardResponse(CARD_ID, USER_ID, "4000000000000001", "ANNA SMITH",
                LocalDate.of(2030, 1, 31), true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createSavesCardForOwnerAndEvictsOwnerFromCache() {
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(4L);
        when(paymentCardMapper.toEntity(request)).thenReturn(card);
        when(paymentCardRepository.save(card)).thenReturn(card);
        when(paymentCardMapper.toResponse(card)).thenReturn(response);
        stubUsersCache();

        PaymentCardResponse result = paymentCardService.create(USER_ID, request);

        assertThat(result).isEqualTo(response);
        assertThat(card.getUser()).isSameAs(user);
        verify(usersCache).evict(USER_ID);
    }

    @Test
    void createThrowsWhenUserDoesNotExist() {
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.create(USER_ID, request))
                .isInstanceOf(UserNotFoundException.class);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenUserAlreadyHasFiveActiveCards() {
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(5L);

        assertThatThrownBy(() -> paymentCardService.create(USER_ID, request))
                .isInstanceOf(CardLimitExceededException.class)
                .hasMessage("User with id 1 already has the maximum of 5 active cards");
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void getAllByUserIdReturnsCardsOfUser() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(paymentCardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(card));
        when(paymentCardMapper.toResponse(card)).thenReturn(response);

        List<PaymentCardResponse> result = paymentCardService.getAllByUserId(USER_ID);

        assertThat(result).containsExactly(response);
    }

    @Test
    void getAllByUserIdThrowsWhenUserDoesNotExist() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> paymentCardService.getAllByUserId(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
        verify(paymentCardRepository, never()).findAllByUserId(anyLong());
    }

    @Test
    void getByIdReturnsCard() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toResponse(card)).thenReturn(response);

        assertThat(paymentCardService.getById(CARD_ID)).isEqualTo(response);
    }

    @Test
    void getByIdThrowsWhenCardDoesNotExist() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.getById(CARD_ID))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Payment card with id 10 not found");
    }

    @Test
    void updateCopiesRequestAndEvictsOwnerFromCache() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(paymentCardRepository.saveAndFlush(card)).thenReturn(card);
        when(paymentCardMapper.toResponse(card)).thenReturn(response);
        stubUsersCache();

        PaymentCardResponse result = paymentCardService.update(CARD_ID, request);

        assertThat(result).isEqualTo(response);
        verify(paymentCardMapper).updateEntity(request, card);
        verify(usersCache).evict(USER_ID);
    }

    @Test
    void updateThrowsWhenCardDoesNotExist() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.update(CARD_ID, request))
                .isInstanceOf(CardNotFoundException.class);
        verify(paymentCardRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteRemovesCardAndEvictsOwnerFromCache() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        stubUsersCache();

        paymentCardService.delete(CARD_ID);

        verify(paymentCardRepository).deleteById(CARD_ID);
        verify(usersCache).evict(USER_ID);
    }

    @Test
    void deleteThrowsWhenCardDoesNotExist() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.delete(CARD_ID))
                .isInstanceOf(CardNotFoundException.class);
        verify(paymentCardRepository, never()).deleteById(anyLong());
    }

    @Test
    void deactivateDoesNotLockOwnerNorCheckLimit() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(paymentCardRepository.updateActiveStatus(CARD_ID, false)).thenReturn(1);
        stubUsersCache();

        paymentCardService.updateStatus(CARD_ID, false);

        verify(userRepository, never()).findByIdForUpdate(anyLong());
        verify(paymentCardRepository, never()).countByUserIdAndActiveTrue(anyLong());
        verify(usersCache).evict(USER_ID);
    }

    @Test
    void activateInactiveCardLocksOwnerAndChecksLimit() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.existsByIdAndActiveTrue(CARD_ID)).thenReturn(false);
        when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(4L);
        when(paymentCardRepository.updateActiveStatus(CARD_ID, true)).thenReturn(1);
        stubUsersCache();

        paymentCardService.updateStatus(CARD_ID, true);

        verify(userRepository).findByIdForUpdate(USER_ID);
        verify(paymentCardRepository).updateActiveStatus(CARD_ID, true);
        verify(usersCache).evict(USER_ID);
    }

    @Test
    void activateInactiveCardThrowsWhenLimitReached() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.existsByIdAndActiveTrue(CARD_ID)).thenReturn(false);
        when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(5L);

        assertThatThrownBy(() -> paymentCardService.updateStatus(CARD_ID, true))
                .isInstanceOf(CardLimitExceededException.class);
        verify(paymentCardRepository, never()).updateActiveStatus(anyLong(), anyBoolean());
    }

    @Test
    void activateAlreadyActiveCardSkipsLimitCheck() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user));
        when(paymentCardRepository.existsByIdAndActiveTrue(CARD_ID)).thenReturn(true);
        when(paymentCardRepository.updateActiveStatus(CARD_ID, true)).thenReturn(1);
        stubUsersCache();

        paymentCardService.updateStatus(CARD_ID, true);

        verify(paymentCardRepository, never()).countByUserIdAndActiveTrue(anyLong());
    }

    @Test
    void updateStatusThrowsWhenCardDoesNotExist() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.updateStatus(CARD_ID, false))
                .isInstanceOf(CardNotFoundException.class);
        verify(paymentCardRepository, never()).updateActiveStatus(anyLong(), anyBoolean());
    }

    @Test
    void updateStatusThrowsWhenCardIsDeletedConcurrently() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(paymentCardRepository.updateActiveStatus(CARD_ID, false)).thenReturn(0);

        assertThatThrownBy(() -> paymentCardService.updateStatus(CARD_ID, false))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void writeSucceedsWhenUsersCacheIsNotAvailable() {
        when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cacheManager.getCache(CacheConfig.USERS_CACHE)).thenReturn(null);

        assertThatCode(() -> paymentCardService.delete(CARD_ID)).doesNotThrowAnyException();
        verify(paymentCardRepository).deleteById(CARD_ID);
    }

    private void stubUsersCache() {
        when(cacheManager.getCache(CacheConfig.USERS_CACHE)).thenReturn(usersCache);
    }
}
