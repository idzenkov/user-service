package org.example.userservice.service;

import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.User;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldSaveAndReturnDto() {
        // given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAge(25);

        User userEntity = new User("John", "john@example.com", 25);
        User savedEntity = new User("John", "john@example.com", 25);
        savedEntity.setId(1L);
        UserDto expectedDto = new UserDto(1L, "John", "john@example.com", 25);

        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        // when
        UserDto result = userService.createUser(request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        verify(userRepository).save(userEntity);
    }

    @Test
    void getUserById_WhenExists_ShouldReturnDto() {
        // given
        User user = new User("Jane", "jane@example.com", 30);
        user.setId(1L);
        UserDto dto = new UserDto(1L, "Jane", "jane@example.com", 30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        // when
        UserDto result = userService.getUserById(1L);

        // then
        assertThat(result.getName()).isEqualTo("Jane");
    }

    @Test
    void getUserById_WhenNotExists_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void getAllUsers_ShouldReturnListOfDto() {
        // given
        User user1 = new User("A", "a@a.com", 20);
        user1.setId(1L);
        User user2 = new User("B", "b@b.com", 25);
        user2.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(new UserDto(1L, "A", "a@a.com", 20));
        when(userMapper.toDto(user2)).thenReturn(new UserDto(2L, "B", "b@b.com", 25));

        // when
        List<UserDto> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A");
    }

    @Test
    void updateUser_WhenExists_ShouldUpdateFieldsAndReturnDto() {
        // given
        User existing = new User("Old", "old@example.com", 30);
        existing.setId(1L);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setAge(31);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        // Мокаем behaviour маппера: он должен изменить переданный объект existing
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            UpdateUserRequest req = invocation.getArgument(1);
            if (req.getName() != null) u.setName(req.getName());
            if (req.getEmail() != null) u.setEmail(req.getEmail());
            if (req.getAge() != null) u.setAge(req.getAge());
            return null;
        }).when(userMapper).updateEntity(any(User.class), any(UpdateUserRequest.class));

        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(new UserDto(1L, "Updated Name", "old@example.com", 31));

        // when
        UserDto result = userService.updateUser(1L, request);

        // then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getAge()).isEqualTo(31);
        verify(userMapper).updateEntity(existing, request);
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_WhenNotExists_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, new UpdateUserRequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteUser_WhenExists_ShouldCallRepositoryDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrow() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}