package de.lenneflow.workflowservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PingControllerTest {

    private PingController pingController;

    @BeforeEach
    void setUp() {
        pingController = new PingController();
    }

    @Test
    void checkService_shouldReturnWelcomeMessage() {
        String result = pingController.checkService();
        assertNotNull(result);
        assertEquals("Welcome to the Workflow Service!", result);
    }
}