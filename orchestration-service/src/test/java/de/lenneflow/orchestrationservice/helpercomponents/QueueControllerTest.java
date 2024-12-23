package de.lenneflow.orchestrationservice.helpercomponents;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.dto.RunNotification;
import de.lenneflow.orchestrationservice.utils.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

class QueueControllerTest {

    @Mock
    private AmqpAdmin admin;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private QueueController queueController;

    MockedStatic<Util> utilities;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueController = new QueueController(admin, rabbitTemplate);
        utilities = Mockito.mockStatic(Util.class);
    }

    @AfterEach
    void tearDown(){
        utilities.close();
    }

    @Test
    void publishRunStateChange_sendsRunNotification() {
        RunNotification runNotification = new RunNotification();
        byte[] serializedNotification = "serializedNotification".getBytes();
        utilities.when(() -> Util.serialize(runNotification)).thenReturn(serializedNotification);

        queueController.publishRunStateChange(runNotification);

        verify(admin).declareExchange(any());
        verify(rabbitTemplate).convertAndSend(QueueController.RUN_STATE_EXCHANGE, QueueController.RUN_STATE_ROUTING, serializedNotification);
    }

    @Test
    void addFunctionDtoToQueue_sendsQueueElement() {
        QueueElement queueElement = new QueueElement();
        byte[] serializedQueueElement = "serializedQueueElement".getBytes();
        utilities.when(() -> Util.serialize(queueElement)).thenReturn(serializedQueueElement);

        queueController.addFunctionDtoToQueue(queueElement);

        verify(admin).declareQueue(any());
        verify(admin).declareExchange(any());
        verify(admin).declareBinding(any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(serializedQueueElement));
    }

    @Test
    void addElementToResultQueue_sendsResultQueueElement() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        byte[] serializedResultQueueElement = "serializedResultQueueElement".getBytes();
        utilities.when(() -> Util.serialize(resultQueueElement)).thenReturn(serializedResultQueueElement);

        queueController.addElementToResultQueue(resultQueueElement);

        verify(admin).declareQueue(any());
        verify(admin).declareExchange(any());
        verify(admin).declareBinding(any());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(serializedResultQueueElement));
    }

    @Test
    void publishRunStateChange_logsErrorOnException() {
        RunNotification runNotification = new RunNotification();
        utilities.when(() -> Util.serialize(runNotification)).thenThrow(new JsonProcessingException("error") {});

        queueController.publishRunStateChange(runNotification);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void addFunctionDtoToQueue_logsErrorOnException() {
        QueueElement queueElement = new QueueElement();
        utilities.when(() -> Util.serialize(queueElement)).thenThrow(new JsonProcessingException("error") {});

        queueController.addFunctionDtoToQueue(queueElement);

        verify(admin, never()).declareQueue(any());
        verify(admin, never()).declareExchange(any());
        verify(admin, never()).declareBinding(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void addElementToResultQueue_logsErrorOnException() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        utilities.when(() -> Util.serialize(resultQueueElement)).thenThrow(new JsonProcessingException("error") {});

        queueController.addElementToResultQueue(resultQueueElement);

        verify(admin, never()).declareQueue(any());
        verify(admin, never()).declareExchange(any());
        verify(admin, never()).declareBinding(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}