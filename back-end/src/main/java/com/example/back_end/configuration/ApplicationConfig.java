package com.example.back_end.configuration;

import com.example.back_end.entity.User;
import static com.example.back_end.enums.UserRole.*;
import com.example.back_end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner applicationRunner (){
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User user = User.builder()
                        .email("admin@example.com")
                        .fullName("System Admin")
                        .isActive(true)
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .role(TEACHER)
                        .build();
                userRepository.save(user);
                log.info("User admin was created, please change password!");
            }
        };
    }

}
