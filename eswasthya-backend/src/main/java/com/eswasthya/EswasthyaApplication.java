package com.eswasthya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/*
 * eSwasthya — Health & Welfare Backend Application
 *
 * <p>RESTful API backend for the eSwasthya health tracking system.
 * Manages user health records, alerts, and admin reporting.</p>
 *
 * @author Azam Khan (adapted to Spring Boot)
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class EswasthyaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EswasthyaApplication.class, args);
    }
}
