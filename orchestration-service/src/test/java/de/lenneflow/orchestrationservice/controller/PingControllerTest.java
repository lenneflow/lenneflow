package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PingControllerTest {

    @Mock
    private FunctionServiceClient functionServiceClient;

    private PingController pingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pingController = new PingController(functionServiceClient);
    }

    @Test
    void checkService_shouldReturnWelcomeMessage() {
        String result = pingController.checkService();
        assertNotNull(result);
        assertEquals("Welcome to the Orchestration Service!", result);
    }

    @Test
    void checkFeign_shouldReturnFunctionHome() {
        String homeString = "home";
        when(functionServiceClient.getFunctionHome()).thenReturn(homeString);

        String result = pingController.checkFeign();

        assertNotNull(result);
        assertEquals(homeString, result);
    }

    @Test
    void checkFeign_shouldHandleNullResponse() {
        when(functionServiceClient.getFunctionHome()).thenReturn(null);

        String result = pingController.checkFeign();

        assertNull(result);
    }

    @Test
    void checkFeign_shouldHandleEmptyResponse() {
        when(functionServiceClient.getFunctionHome()).thenReturn("");

        String result = pingController.checkFeign();

        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void checkFeign_shouldThrowExceptionOnRestTemplateError() {
        when(functionServiceClient.getFunctionHome()).thenThrow(new RuntimeException("RestTemplate error"));

        assertThrows(RuntimeException.class, () -> pingController.checkFeign());
    }
}