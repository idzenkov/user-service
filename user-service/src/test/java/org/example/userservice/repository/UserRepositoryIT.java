package org.example.userservice.repository;

import org.example.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // отключаем встроенную H2
class UserRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void save_ShouldPersistUser() {
        User user = new User("Test", "test@example.com", 30);
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        User found = entityManager.find(User.class, saved.getId());
        assertThat(found.getName()).isEqualTo("Test");
    }

    @Test
    void findById_ShouldReturnUser() {
        User user = new User("Find", "find@ex.com", 25);
        User saved = entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("find@ex.com");
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        User user = new User("Email", "unique@ex.com", 40);
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("unique@ex.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Email");
    }

    @Test
    void update_ShouldModifyUser() {
        User user = new User("Old", "old@ex.com", 50);
        User saved = entityManager.persistAndFlush(user);

        saved.setName("New");
        saved.setAge(51);
        userRepository.save(saved);
        entityManager.flush();

        User updated = entityManager.find(User.class, saved.getId());
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getAge()).isEqualTo(51);
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = new User("Del", "del@ex.com", 60);
        User saved = entityManager.persistAndFlush(user);

        userRepository.deleteById(saved.getId());
        entityManager.flush();

        User found = entityManager.find(User.class, saved.getId());
        assertThat(found).isNull();
    }
}