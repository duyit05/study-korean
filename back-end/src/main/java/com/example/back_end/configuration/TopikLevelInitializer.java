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

    }
}
