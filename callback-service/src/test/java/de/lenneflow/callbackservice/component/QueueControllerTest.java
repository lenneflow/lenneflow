package de.lenneflow.callbackservice.component;

import de.lenneflow.callbackservice.config.AppConfiguration;
import de.lenneflow.callbackservice.dto.ResultQueueElement;
import de.lenneflow.callbackservice.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueController = new QueueController(admin, rabbitTemplate);
    }

    @Test
    void addFunctionDtoToResultQueue_shouldCreateQueueAndSendMessage() {
        ResultQueueElement functionDto = new ResultQueueElement();
        byte[] serializedFunctionDto = Util.serializeResultQueueElement(functionDto);
        String queueName = AppConfiguration.RESULTSQUEUENAME;
        String exchange = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";

        queueController.addFunctionDtoToResultQueue(functionDto);

        verify(admin).declareQueue(any());
        verify(admin).declareExchange(any());
        verify(admin).declareBinding(any());
        verify(rabbitTemplate).convertAndSend(exchange, routingKey, serializedFunctionDto);
    }

}