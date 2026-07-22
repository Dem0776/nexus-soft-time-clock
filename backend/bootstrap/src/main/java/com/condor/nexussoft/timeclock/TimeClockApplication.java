package com.condor.nexussoft.timeclock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada del modular monolith (ADR-001). El escaneo de componentes cubre
 * {@code com.condor.nexussoft.timeclock.*}, por lo que cada bounded context añadido en
 * iteraciones posteriores se autoconfigura sin tocar esta clase.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TimeClockApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeClockApplication.class, args);
    }
}
