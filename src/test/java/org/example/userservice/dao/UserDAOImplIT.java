package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOImplIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private SessionFactory sessionFactory;
    private UserDAO userDAO;

    @BeforeAll
    void setUp() {
        // Настраиваем Hibernate с параметрами Testcontainers
        Configuration configuration = new Configuration().configure();
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop"); // чистая БД для каждого теста
        configuration.addAnnotatedClass(User.class);
        sessionFactory = configuration.buildSessionFactory();

        userDAO = new UserDAOImpl(sessionFactory);
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        // Очищаем таблицу перед каждым тестом
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void save_ShouldPersistUser() {
        User user = new User("Test", "test@mail.com", 25);
        userDAO.save(user);
        assertNotNull(user.getId());
        // Проверим через отдельную сессию
        try (Session session = sessionFactory.openSession()) {
            User found = session.get(User.class, user.getId());
            assertNotNull(found);
            assertEquals("test@mail.com", found.getEmail());
        }
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        User user = new User("Find", "find@ex.com", 30);
        userDAO.save(user);
        Optional<User> found = userDAO.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Find", found.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        Optional<User> found = userDAO.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userDAO.save(new User("A", "a@a.com", 20));
        userDAO.save(new User("B", "b@b.com", 25));
        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldModifyUser() {
        User user = new User("Old", "old@ex.com", 40);
        userDAO.save(user);
        user.setName("New Name");
        user.setAge(41);
        userDAO.update(user);
        Optional<User> updated = userDAO.findById(user.getId());
        assertEquals("New Name", updated.get().getName());
        assertEquals(41, updated.get().getAge());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = new User("Del", "del@ex.com", 50);
        userDAO.save(user);
        assertTrue(userDAO.findById(user.getId()).isPresent());
        userDAO.deleteById(user.getId());
        assertFalse(userDAO.findById(user.getId()).isPresent());
    }

    @Test
    void delete_ShouldRemoveUserEntity() {
        User user = new User("DelEnt", "ent@ex.com", 55);
        userDAO.save(user);
        userDAO.delete(user);
        assertFalse(userDAO.findById(user.getId()).isPresent());
    }
}