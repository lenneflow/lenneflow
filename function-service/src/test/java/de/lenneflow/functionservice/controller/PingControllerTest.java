package de.lenneflow.functionservice.controller;

import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PingControllerTest {

    @Mock
    private WorkerServiceClient workerServiceClient;

    private PingController pingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pingController = new PingController(workerServiceClient);
    }

    @Test
    void checkService_shouldReturnWelcomeMessage() {
        String result = pingController.checkService();
        assertEquals("Welcome to the Function Service! Everything is working fine!", result);
    }

    @Test
    void checkFeignService_shouldReturnFeignClientResponse() {
        when(workerServiceClient.ping()).thenReturn("Feign client is working!");
        String result = pingController.checkFeignService();
        assertEquals("Feign client is working!", result);
    }

}