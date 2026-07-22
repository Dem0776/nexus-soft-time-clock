package com.condor.nexussoft.timeclock.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de humo del endpoint /api/v1/ping mediante MockMvc standalone
 * (sin cargar el contexto completo ni requerir base de datos).
 */
class PingControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PingController()).build();

    @Test
    void ping_returns_status_up() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.serverTime").exists());
    }
}
