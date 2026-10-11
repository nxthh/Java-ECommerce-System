package org.marketplace.repository;

import org.marketplace.model.TableData;
import org.marketplace.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserRepository {

    User findByUsername(String username) throws SQLException;

    void save(User user) throws SQLException;

    TableData findById(long id) throws SQLException;

    List<User> findAll() throws SQLException;

    int updateName(long id, String fullName) throws SQLException;

    void lock(Connection connection, long userId) throws SQLException;
}