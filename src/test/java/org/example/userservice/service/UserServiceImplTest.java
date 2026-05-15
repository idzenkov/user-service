package org.example.userservice.service;


import org.example.userservice.dao.UserDAO;
import org.example.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", 30);
        testUser.setId(1L);
    }

    @Test
    void createUser_ShouldSaveUser_WhenValidData() {
        // given
        String name = "Alice";
        String email = "alice@example.com";
        Integer age = 25;
        User userToSave = new User(name, email, age);
        // when
        userService.createUser(name, email, age);
        // then
        verify(userDAO, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("", "email@e.com", 20));
        verifyNoInteractions(userDAO);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Name", "", 20));
        verifyNoInteractions(userDAO);
    }

    @Test
    void createUser_ShouldThrowException_WhenAgeOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Name", "e@e.com", -5));
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("Name", "e@e.com", 200));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userDAO).findById(1L);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenNotExists() {
        when(userDAO.findById(99L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(99L);
        assertFalse(result.isPresent());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<User> users = Arrays.asList(testUser, new User("Jane", "jane@ex.com", 28));
        when(userDAO.findAll()).thenReturn(users);
        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
        verify(userDAO).findAll();
    }

    @Test
    void updateUser_ShouldUpdate_WhenUserExists() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        userService.updateUser(1L, "Updated Name", null, null);
        assertEquals("Updated Name", testUser.getName());
        verify(userDAO).update(testUser);
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userDAO.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                userService.updateUser(1L, "Name", "email", 25));
    }

    @Test
    void deleteUser_ShouldCallDeleteById() {
        userService.deleteUser(5L);
        verify(userDAO).deleteById(5L);
    }
}