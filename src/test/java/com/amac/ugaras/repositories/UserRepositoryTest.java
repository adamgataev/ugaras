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
        String existingEmail = "peter.pech@hotmail.com";

        Optional<User> userOptional = userRepository.findByEmail(existingEmail);

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getEmail()).isEqualTo(existingEmail);

        System.out.println("Gevonden User ID: " + userOptional.get().getId());
    }

    @Test
    @Rollback(false) // Resultaat in de DB zien (optioneel)
    void testApplySoftDelete() {
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password(8, 20);

        System.out.println("Testen met fake user: " + randomEmail);

        User user = User.builder()
                .email(randomEmail)
                .passwordHash(randomPassword)
                .enabled(true)
                .build();
        // User aanmaken
        userRepository.save(user);

        // User zoeken
        Optional<User> foundUser = userRepository.findByEmail(randomEmail);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(randomEmail);

        // User (soft) verwijderen
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        // Hier mag hij niet gevonden kunnen worden
        Optional<User> deletedUser = userRepository.findByEmail(randomEmail);
        assertThat(deletedUser).isEmpty();
    }
}
