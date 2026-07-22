package com.condor.nexussoft.timeclock.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint trivial de vida del skeleton (Iteración 4). Confirma que el stack web,
 * la versión de la app y la hora de servidor (ADR-003) están operativos.
 */
@RestController
@RequestMapping("/api/v1")
public class PingController {

    @Value("${spring.application.name:nexus-time-clock}")
    private String appName = "nexus-time-clock";  // default no nulo si no hay contexto Spring

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "app", appName,
                "status", "UP",
                "serverTime", Instant.now().toString()
        );
    }
}
