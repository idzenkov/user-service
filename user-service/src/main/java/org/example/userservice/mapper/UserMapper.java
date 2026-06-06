package org.example.userservice.mapper;

import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }

    public User toEntity(CreateUserRequest request) {
        if (request == null) return null;
        return new User(request.getName(), request.getEmail(), request.getAge());
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAge() != null) user.setAge(request.getAge());
    }
}