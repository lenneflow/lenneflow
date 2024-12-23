package de.lenneflow.callbackservice.controller;

import de.lenneflow.callbackservice.component.QueueController;
import de.lenneflow.callbackservice.dto.ResultQueueElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CallBackControllerTest {

    @Mock
    private QueueController queueController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CallBackController callBackController = new CallBackController(queueController);
        mockMvc = MockMvcBuilders.standaloneSetup(callBackController).build();
    }


    @Test
    void workerCallBack_shouldHandleNullPayload() throws Exception {
        mockMvc.perform(post("/api/callback/step1/workflow1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verify(queueController, times(0)).addFunctionDtoToResultQueue(any(ResultQueueElement.class));
    }
}