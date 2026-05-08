package org.example.userservice.service;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.dao.UserDAOImpl;
import org.example.userservice.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    // Для тестирования можно передать mock
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void createUser(String name, String email, Integer age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        User user = new User(name, email, age);
        userDAO.save(user);
        logger.info("User created via service: {}", user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        return userDAO.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public void updateUser(Long id, String name, String email, Integer age) {
        User user = userDAO.findById(id).orElseThrow(() ->
                new RuntimeException("User not found with id: " + id));
        if (name != null && !name.isBlank()) user.setName(name);
        if (email != null && !email.isBlank()) user.setEmail(email);
        if (age != null) {
            if (age < 0 || age > 150) throw new IllegalArgumentException("Invalid age");
            user.setAge(age);
        }
        userDAO.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDAO.deleteById(id);
    }
}