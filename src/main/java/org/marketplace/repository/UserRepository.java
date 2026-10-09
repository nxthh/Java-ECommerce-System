package org.marketplace.repository;

import org.marketplace.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link User} entities.
 */
public interface UserRepository {

    /** Finds a user by their unique username. */
    Optional<User> findByUsername(String username);

    /** Finds a user by their primary key. */
    Optional<User> findById(long id);

    /** Returns every user ordered by id. */
    List<User> findAll();

    /** Returns all users with the given role. */
    List<User> findByRole(User.Role role);

    /**
     * Persists a new user.
     *
     * @return the saved user with its generated {@code id}.
     */
    User save(User user);

    /**
     * Updates an existing user's profile fields.
     *
     * @return {@code true} if a row was updated.
     */
    boolean update(User user);

    /**
     * Deletes a user by id.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean delete(long id);
}
