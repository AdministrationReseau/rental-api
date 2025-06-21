package inc.yowyob.rental_api.user.config;

import inc.yowyob.rental_api.core.enums.UserStatus;
import inc.yowyob.rental_api.core.enums.UserType;
import inc.yowyob.rental_api.user.entities.User;
import inc.yowyob.rental_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialise les données de base pour les utilisateurs
 */
@Slf4j
@Component
@Order(2) // Après SubscriptionDataInitializer
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing user data...");

        try {
            createSuperAdminUser();
            log.info("User data initialization completed successfully.");

        } catch (Exception e) {
            log.error("Error during user data initialization: {}", e.getMessage(), e);
        }
    }

    private void createSuperAdminUser() {
        String adminEmail = "admin@rental-api.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Creating super admin user...");

            User admin = new User(
                adminEmail,
                passwordEncoder.encode("Admin123!"),
                "Super",
                "Admin",
                UserType.SUPER_ADMIN
            );

            admin.setStatus(UserStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setPhoneVerified(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            log.info("Super admin user created successfully with email: {}", adminEmail);
        } else {
            log.info("Super admin user already exists. Skipping creation.");
        }
    }
}
