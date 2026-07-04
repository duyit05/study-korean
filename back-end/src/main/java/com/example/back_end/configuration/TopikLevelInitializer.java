package com.example.back_end.configuration;

import com.example.back_end.entity.TopikLevel;
import com.example.back_end.repository.TopikLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopikLevelInitializer implements CommandLineRunner {

    private final TopikLevelRepository topikLevelRepository;

    @Override
    public void run(String... args) throws Exception {
        if (topikLevelRepository.count() == 0) {
            log.info("Seeding default TOPIK levels...");
            topikLevelRepository.saveAll(List.of(
                TopikLevel.builder().name("Sơ cấp 1A").code("SO_CAP_1A").groupType("VOCAB").build(),
                TopikLevel.builder().name("Sơ cấp 1B").code("SO_CAP_1B").groupType("VOCAB").build(),
                TopikLevel.builder().name("Trung cấp (3-4)").code("TRUNG_CAP").groupType("VOCAB").build(),
                TopikLevel.builder().name("Cao cấp (5-6)").code("CAO_CAP").groupType("VOCAB").build(),
                TopikLevel.builder().name("TOPIK I (Nghe & Đọc)").code("TOPIK_I").groupType("QUIZ").build(),
                TopikLevel.builder().name("TOPIK II (Nghe, Đọc & Viết)").code("TOPIK_II").groupType("QUIZ").build(),
                TopikLevel.builder().name("Lớp 1A").code("LOP_1A").groupType("QUIZ").build(),
                TopikLevel.builder().name("Lớp 1B").code("LOP_1B").groupType("QUIZ").build()
            ));
            log.info("TOPIK levels seeded successfully.");
        }
    }
}
