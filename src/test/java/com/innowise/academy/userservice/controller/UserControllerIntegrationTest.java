package com.innowise.academy.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerIntegrationTest {

    private static final String USER_JSON = """
            {
              "name": "Anna",
              "surname": "Schmidt",
              "birthDate": "1995-04-12",
              "email": "anna@test.com"
            }
            """;

    private static final String CARD_JSON = """
            {
              "number": "4111111111111111",
              "holder": "ANNA SCHMIDT",
              "expirationDate": "2030-12-31"
            }
            """;

    private static final long UNKNOWN_ID = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
        usersCache().clear();
    }

    // ---------- POST /api/users ----------

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("anna@test.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "surname": "Schmidt",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void createWithDuplicateEmailReturns409() throws Exception {
        createUser(USER_JSON);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user with this email already exists"));
    }

    // ---------- GET /api/users ----------

    @Test
    void getAllFiltersByNameAndSurnameIgnoringCase() throws Exception {
        createUser(userJson("Anna", "Schmidt", "anna@test.com"));
        createUser(userJson("Hanna", "Meyer", "hanna@test.com"));
        createUser(userJson("Peter", "Schmidt", "peter@test.com"));

        mockMvc.perform(get("/api/users")
                        .param("name", "ANN")
                        .param("surname", "schmi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("anna@test.com"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void getAllReturnsRequestedPage() throws Exception {
        createUser(userJson("Anna", "Schmidt", "anna@test.com"));
        createUser(userJson("Hanna", "Meyer", "hanna@test.com"));
        createUser(userJson("Peter", "Schmidt", "peter@test.com"));

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("peter@test.com"))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    // ---------- GET /api/users/{id} ----------

    @Test
    void getByIdReturnsUserWithCards() throws Exception {
        long id = createUser(USER_JSON);
        mockMvc.perform(post("/api/users/{userId}/cards", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CARD_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Anna"))
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].holder").value("ANNA SCHMIDT"));
    }

    @Test
    void getByUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User with id 999999 not found"))
                .andExpect(jsonPath("$.path").value("/api/users/999999"));
    }

    @Test
    void getByNonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/users/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Parameter 'id' has an invalid value 'abc'"));
    }

    // ---------- PUT /api/users/{id} ----------

    @Test
    void updateReturns200WithNewData() throws Exception {
        long id = createUser(USER_JSON);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Anne", "Schmitt", "anne@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anne"))
                .andExpect(jsonPath("$.surname").value("Schmitt"))
                .andExpect(jsonPath("$.email").value("anne@test.com"));
    }

    @Test
    void updateUnknownUserReturns404() throws Exception {
        mockMvc.perform(put("/api/users/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /api/users/{id} ----------

    @Test
    void deleteReturns204AndUserIsGone() throws Exception {
        long id = createUser(USER_JSON);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownUserReturns404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/users/{id}/status ----------

    @Test
    void updateStatusReturns204AndChangesActive() throws Exception {
        long id = createUser(USER_JSON);

        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateStatusWithoutActiveReturns400() throws Exception {
        long id = createUser(USER_JSON);

        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.active").exists());
    }

    // ---------- Unknown endpoint ----------

    @Test
    void unknownEndpointReturns404() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", endsWith("/api/unknown")));
    }

    // ---------- Redis cache ----------

    @Test
    void getByIdStoresUserInCacheAndServesItFromThere() throws Exception {
        long id = createUser(USER_JSON);
        assertThat(usersCache().get(id)).isNull();

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.name").value("Anna"));
        assertThat(usersCache().get(id)).isNotNull();

        // Change the database behind the service's back: the cached copy must still be returned.
        userRepository.findById(id).ifPresent(user -> {
            user.setName("Changed");
            userRepository.save(user);
        });

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void updateEvictsUserFromCache() throws Exception {
        long id = createUser(USER_JSON);
        mockMvc.perform(get("/api/users/{id}", id));
        assertThat(usersCache().get(id)).isNotNull();

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Anne", "Schmidt", "anna@test.com")))
                .andExpect(status().isOk());

        assertThat(usersCache().get(id)).isNull();
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.name").value("Anne"));
    }

    @Test
    void addingCardEvictsOwnerFromCache() throws Exception {
        long id = createUser(USER_JSON);
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.cards.length()").value(0));

        mockMvc.perform(post("/api/users/{userId}/cards", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CARD_JSON))
                .andExpect(status().isCreated());

        assertThat(usersCache().get(id)).isNull();
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.cards.length()").value(1));
    }

    // ---------- Helpers ----------

    private long createUser(String json) throws Exception {
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

    private static String userJson(String name, String surname, String email) {
        return """
                {
                  "name": "%s",
                  "surname": "%s",
                  "email": "%s"
                }
                """.formatted(name, surname, email);
    }

    private Cache usersCache() {
        return cacheManager.getCache(CacheConfig.USERS_CACHE);
    }
}
