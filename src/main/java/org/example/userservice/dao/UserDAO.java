package org.example.userservice.dao;

import org.example.userservice.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    void save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void update(User user);
    void delete(User user);
    void deleteById(Long id);
}
