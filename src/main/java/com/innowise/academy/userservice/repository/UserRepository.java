package com.innowise.academy.userservice.repository;

import com.innowise.academy.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
     * Activates or deactivates a user with a single JPQL update.
     *
     * @param id     the user id
     * @param active the new status
     * @return the number of updated rows (0 if the user does not exist)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.active = :active where u.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);
}
