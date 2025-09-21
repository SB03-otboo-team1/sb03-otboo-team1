package com.onepiece.otboo.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationPropertiesScan(basePackages = "com.onepiece.otboo")
public class PropertiesScanConfig {

    @PostConstruct
    public void init() {
        log.info("[PropertiesScanConfig] initialized");
    }
}