package com.onepiece.otboo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OtbooApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtbooApplication.class, args);
    }

}
