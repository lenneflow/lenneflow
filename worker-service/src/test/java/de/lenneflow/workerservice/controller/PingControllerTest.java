package de.lenneflow.workerservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PingController.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void checkService_shouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/workers/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to the Worker Service!"));
    }

    @Test
    void checkService_shouldReturnNotFoundForInvalidEndpoint() throws Exception {
        mockMvc.perform(get("/api/workers/invalid"))
                .andExpect(status().isNotFound());
    }
}