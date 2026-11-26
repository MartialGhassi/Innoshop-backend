package com.innowise.academy.userservice.controller;

import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.academy.userservice.TestcontainersConfiguration;
import com.innowise.academy.userservice.config.CacheConfig;
import com.innowise.academy.userservice.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PaymentCardControllerIntegrationTest {

    private static final String USER_JSON = """
            {
              "name": "Anna",
              "surname": "Schmidt",
              "email": "anna@test.com"
            }
            """;

    private static final String CARD_NUMBER = "4111111111111111";

    private static final long UNKNOWN_ID = 999_999L;

    private static final int MAX_ACTIVE_CARDS = 5;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private long userId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        cacheManager.getCache(CacheConfig.USERS_CACHE).clear();
        userId = createUser();
    }

    // ---------- POST /api/users/{userId}/cards ----------

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(CARD_NUMBER)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesRegex(".*/api/cards/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.number").value(CARD_NUMBER))
                .andExpect(jsonPath("$.holder").value("ANNA SCHMIDT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createForUnknownUserReturns404() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/cards", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(CARD_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 999999 not found"));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors() throws Exception {
        String invalidJson = """
                {
                  "number": "",
                  "holder": "ANNA SCHMIDT",
                  "expirationDate": "2020-01-01"
                }
                """;

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.number").exists())
                .andExpect(jsonPath("$.fieldErrors.expirationDate").exists());
    }

    @Test
    void createWithDuplicateNumberReturns409() throws Exception {
        createCard(CARD_NUMBER);

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(CARD_NUMBER)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A payment card with this number already exists"));
    }

    @Test
    void createSixthActiveCardReturns409() throws Exception {
        createActiveCards(MAX_ACTIVE_CARDS);

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000000000000099")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with id " + userId
                        + " already has the maximum of 5 active cards"));
    }

    // ---------- GET /api/users/{userId}/cards ----------

    @Test
    void getAllByUserIdReturnsUserCards() throws Exception {
        createCard("4000000000000001");
        createCard("4000000000000002");

        mockMvc.perform(get("/api/users/{userId}/cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    void getAllByUnknownUserReturns404() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/cards", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/cards ----------

    @Test
    void getAllFiltersByOwnerNameAndSurnameIgnoringCase() throws Exception {
        createCard("4000000000000001");
        createCardFor(createUser("Hanna", "Meyer", "hanna@test.com"), "4000000000000002");
        createCardFor(createUser("Peter", "Schmidt", "peter@test.com"), "4000000000000003");

        mockMvc.perform(get("/api/cards")
                        .param("name", "ANN")
                        .param("surname", "schmi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void getAllWithOnlyNameFilterMatchesPartialNames() throws Exception {
        createCard("4000000000000001");
        createCardFor(createUser("Hanna", "Meyer", "hanna@test.com"), "4000000000000002");
        createCardFor(createUser("Peter", "Schmidt", "peter@test.com"), "4000000000000003");

        mockMvc.perform(get("/api/cards").param("name", "anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void getAllWithoutFiltersReturnsRequestedPage() throws Exception {
        createCard("4000000000000001");
        createCardFor(createUser("Hanna", "Meyer", "hanna@test.com"), "4000000000000002");
        createCardFor(createUser("Peter", "Schmidt", "peter@test.com"), "4000000000000003");

        mockMvc.perform(get("/api/cards")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].number").value("4000000000000003"))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    // ---------- GET /api/cards/{id} ----------

    @Test
    void getByIdReturnsCard() throws Exception {
        long cardId = createCard(CARD_NUMBER);

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.number").value(CARD_NUMBER));
    }

    @Test
    void getByUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Payment card with id 999999 not found"));
    }

    // ---------- PUT /api/cards/{id} ----------

    @Test
    void updateReturns200WithNewData() throws Exception {
        long cardId = createCard(CARD_NUMBER);
        String newCard = """
                {
                  "number": "5500000000000004",
                  "holder": "ANNE SCHMITT",
                  "expirationDate": "2031-06-30"
                }
                """;

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCard))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("5500000000000004"))
                .andExpect(jsonPath("$.holder").value("ANNE SCHMITT"))
                .andExpect(jsonPath("$.expirationDate").value("2031-06-30"));
    }

    @Test
    void updateUnknownCardReturns404() throws Exception {
        mockMvc.perform(put("/api/cards/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(CARD_NUMBER)))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /api/cards/{id} ----------

    @Test
    void deleteReturns204AndCardIsGone() throws Exception {
        long cardId = createCard(CARD_NUMBER);

        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownCardReturns404() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingUserDeletesItsCards() throws Exception {
        long cardId = createCard(CARD_NUMBER);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/cards/{id}/status ----------

    @Test
    void updateStatusReturns204AndChangesActive() throws Exception {
        long cardId = createCard(CARD_NUMBER);

        mockMvc.perform(patch("/api/cards/{id}/status", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(false)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activatingCardBeyondLimitReturns409() throws Exception {
        long inactiveCardId = createCard("4000000000000099");
        setStatus(inactiveCardId, false);
        createActiveCards(MAX_ACTIVE_CARDS);

        mockMvc.perform(patch("/api/cards/{id}/status", inactiveCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(true)))
                .andExpect(status().isConflict());
    }

    @Test
    void reactivatingAlreadyActiveCardAtLimitReturns204() throws Exception {
        createActiveCards(MAX_ACTIVE_CARDS - 1);
        long fifthCardId = createCard("4000000000000099");

        mockMvc.perform(patch("/api/cards/{id}/status", fifthCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(true)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivatedCardFreesASlotForANewOne() throws Exception {
        createActiveCards(MAX_ACTIVE_CARDS - 1);
        long fifthCardId = createCard("4000000000000099");
        setStatus(fifthCardId, false);

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson("4000000000000098")))
                .andExpect(status().isCreated());
    }

    @Test
    void updateStatusOfUnknownCardReturns404() throws Exception {
        mockMvc.perform(patch("/api/cards/{id}/status", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(false)))
                .andExpect(status().isNotFound());
    }

    // ---------- Helpers ----------

    private long createUser() throws Exception {
        return createUserFromJson(USER_JSON);
    }

    private long createUser(String name, String surname, String email) throws Exception {
        return createUserFromJson("""
                {
                  "name": "%s",
                  "surname": "%s",
                  "email": "%s"
                }
                """.formatted(name, surname, email));
    }

    private long createUserFromJson(String json) throws Exception {
        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        return id.longValue();
    }

    private long createCard(String number) throws Exception {
        return createCardFor(userId, number);
    }

    private long createCardFor(long ownerId, String number) throws Exception {
        String body = mockMvc.perform(post("/api/users/{userId}/cards", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(number)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        return id.longValue();
    }

    private void createActiveCards(int count) throws Exception {
        for (int i = 1; i <= count; i++) {
            createCard("40000000000000%02d".formatted(i));
        }
    }

    private void setStatus(long cardId, boolean active) throws Exception {
        mockMvc.perform(patch("/api/cards/{id}/status", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(active)))
                .andExpect(status().isNoContent());
    }

    private static String cardJson(String number) {
        return """
                {
                  "number": "%s",
                  "holder": "ANNA SCHMIDT",
                  "expirationDate": "2030-12-31"
                }
                """.formatted(number);
    }

    private static String statusJson(boolean active) {
        return "{\"active\": %b}".formatted(active);
    }
}
