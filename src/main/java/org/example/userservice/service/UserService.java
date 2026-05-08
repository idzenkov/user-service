package org.example.userservice.service;

import org.example.userservice.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    void createUser(String name, String email, Integer age);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    void updateUser(Long id, String name, String email, Integer age);
    void deleteUser(Long id);
}