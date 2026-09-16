package com.cyboul.eda.userservice;

import com.cyboul.eda.common.events.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DevDataSeeder(UserRepository userRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<User> seeds = List.of(
                User.builder().name("Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("adminpass"))
                        .build(),
                User.builder().name("User One")
                        .email("user1@example.com")
                        .password(passwordEncoder.encode("userpass1"))
                        .build()
        );

        for (User seed : seeds) {
            if (userRepository.existsByEmail(seed.getEmail())) {
                log.info("DevDataSeeder: skipping {}, email already exists", seed.getName());
                continue;
            }
            User saved = userRepository.save(seed);
            kafkaTemplate.send("user-events", saved.getId(),
                            new UserCreatedEvent(saved.getId(), saved.getEmail(), saved.getName()))
                    .whenComplete((r, ex) -> {
                        if (ex != null) log.error("DevDataSeeder: failed to publish UserCreatedEvent for {}", saved.getName(), ex);
                        else log.info("DevDataSeeder: seeded user {}", saved.getName());
                    });
        }
    }
}
