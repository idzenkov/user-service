package org.example.userservice.service;

import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    UserDto updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}