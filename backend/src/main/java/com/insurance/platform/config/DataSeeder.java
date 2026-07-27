package com.insurance.platform.config;

import com.insurance.platform.entity.PolicyCategory;
import com.insurance.platform.entity.Role;
import com.insurance.platform.entity.User;
import com.insurance.platform.repository.PolicyCategoryRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a default administrator account, a starter set of insurance
 * categories, and default system settings on first startup so the
 * application is immediately usable after deployment.
 *
 * Default admin credentials:
 *   email:    admin@insurance.com
 *   password: Admin@123
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final SystemSettingsService systemSettingsService;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedPolicyCategories();
        systemSettingsService.getOrCreateDefault();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@insurance.com")) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email("admin@insurance.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println("=================================================");
            System.out.println(" Default admin created -> admin@insurance.com / Admin@123");
            System.out.println("=================================================");
        }
    }

    private void seedPolicyCategories() {
        if (policyCategoryRepository.count() > 0) {
            return;
        }
        List<PolicyCategory> defaults = List.of(
                PolicyCategory.builder().name("Health").description("Medical and hospitalization coverage").active(true).build(),
                PolicyCategory.builder().name("Life").description("Life insurance and term plans").active(true).build(),
                PolicyCategory.builder().name("Motor").description("Car and two-wheeler insurance").active(true).build(),
                PolicyCategory.builder().name("Home").description("Home and property insurance").active(true).build(),
                PolicyCategory.builder().name("Travel").description("Domestic and international travel insurance").active(true).build(),
                PolicyCategory.builder().name("Marine").description("Cargo and marine insurance").active(true).build()
        );
        policyCategoryRepository.saveAll(defaults);
    }
}
