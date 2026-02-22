package com.amac.ugaras.repositories;

import com.amac.ugaras.models.entities.User;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// Dit zorgt ervoor dat hij NIET H2 gebruikt, maar jouw application.properties (Postgres)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final Faker faker = new Faker();

    private String generateRandomEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@ugaras-test.com";
    }

    @Test
    void testFindUserByEmail() {
        String phone = "+316" + faker.number().digits(8);
        String email = generateRandomEmail();
        User user = User.builder()
                .phoneNumber(phone)
                .email(email)
                .passwordHash(faker.internet().password(8, 20))
                .enabled(true)
                .build();
        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByEmail(email);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getPhoneNumber()).isEqualTo(phone);
    }

    @Test
    void testFindByPhoneNumber() {
        String phone = "+316" + faker.number().digits(8);
        String email = generateRandomEmail();
        User user = User.builder()
                .phoneNumber(phone)
                .email(email)
                .passwordHash(faker.internet().password(8, 20))
                .enabled(true)
                .build();
        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByPhoneNumber(phone);
        assertThat(found).isPresent();
        assertThat(found.get().getPhoneNumber()).isEqualTo(phone);
    }

    @Test
    @Rollback(false)
    void testApplySoftDelete() {
        String randomPhone = "+316" + faker.number().digits(8);
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password(8, 20);

        User user = User.builder()
                .phoneNumber(randomPhone)
                .email(randomEmail)
                .passwordHash(randomPassword)
                .enabled(true)
                .build();
        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail(randomEmail);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(randomEmail);

        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        Optional<User> deletedUser = userRepository.findByEmail(randomEmail);
        assertThat(deletedUser).isEmpty();
    }
}
