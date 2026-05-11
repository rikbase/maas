package com.maas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaasApplication.class, args);
    }
}
