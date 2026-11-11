package com.innowise.academy.userservice.repository;

import com.innowise.academy.userservice.model.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for {@link User} entities.
 *
 * <p>Paginated filtering is done through {@link JpaSpecificationExecutor} with the
 * specifications from {@code UserSpecification}.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Loads a user and locks its row until the end of the current transaction
     * ({@code SELECT ... FOR UPDATE}).
     *
     * <p>Used to serialize concurrent card operations of the same user, so that
     * the "maximum 5 active cards" check cannot be bypassed by parallel requests.
     *
     * @param id the user id
     * @return the locked user, empty if it does not exist
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * Activates or deactivates a user with a single JPQL update.
     *
     * <p>Bulk updates bypass JPA auditing, so {@code updatedAt} is set explicitly.
     *
     * @param id     the user id
     * @param active the new status
     * @return the number of updated rows (0 if the user does not exist)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.active = :active, u.updatedAt = CURRENT_TIMESTAMP "
            + "where u.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);
}
