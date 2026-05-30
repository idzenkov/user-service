package org.example.userservice.service;

import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserDto;
import org.example.userservice.dto.UserEvent;
import org.example.userservice.entity.User;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        log.info("User created with id: {}", saved.getId());
        kafkaTemplate.send("user-notifications", new UserEvent("CREATE", saved.getEmail()));
        return userMapper.toDto(saved);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
        log.info("User deleted with email: {}", user.getEmail());
        kafkaTemplate.send("user-notifications", new UserEvent("DELETE", user.getEmail()));
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userMapper.updateEntity(user, request);
        User updated = userRepository.save(user);
        log.info("User updated: {}", updated.getEmail());
        return userMapper.toDto(updated);
    }
}