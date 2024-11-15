package de.lenneflow.orchestrationservice.helpercomponents;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.dto.RunNotification;
import de.lenneflow.orchestrationservice.utils.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Controller for all queue events.
 *
 * @author Idrissa Ganemtore
 */
@Component
@EnableRabbit
@RequiredArgsConstructor
public class QueueController {

    private static final Logger logger = LoggerFactory.getLogger(QueueController.class);

    public static final String FUNCTION_RESULT_QUEUE = "functionResultQueue";
    public static final String FUNCTION_QUEUE = "functionQueue";
    public static final String RUN_STATE_QUEUE = "runStateQueue";
    public static final String RUN_STATE_EXCHANGE = "runStateExchange";
    public static final String RUN_STATE_ROUTING = "runStateRouting";

    final AmqpAdmin admin;
    final RabbitTemplate rabbitTemplate;


    public void publishRunStateChange(RunNotification runNotification) {
        try {
            FanoutExchange exchange = new FanoutExchange(QueueController.RUN_STATE_EXCHANGE, false, true);
            admin.declareExchange(exchange);
            rabbitTemplate.convertAndSend(RUN_STATE_EXCHANGE, RUN_STATE_ROUTING, Util.serialize(runNotification));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Adds a function object to the function queue.
     *
     * @param queueElement the function object
     */
    public void addFunctionDtoToQueue(QueueElement queueElement) {
        try {
            byte[] serializedFunctionDto = Util.serialize(queueElement);
            String queueName = FUNCTION_QUEUE;
            String exchange = queueName + "-Exchange";
            String routingKey = queueName + "-RoutingKey";
            createTopicExchangeQueue(queueName, exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Adds a function object to the function results queue.
     *
     * @param resultQueueElement the function object
     */
    public void addElementToResultQueue(ResultQueueElement resultQueueElement) {
        try {
            byte[] serializedFunctionDto = Util.serialize(resultQueueElement);
            String queueName = FUNCTION_RESULT_QUEUE;
            String exchange = queueName + "-Exchange";
            String routingKey = queueName + "-RoutingKey";
            createTopicExchangeQueue(queueName, exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * Create a rabbitmq queue and the corresponding binding.
     *
     * @param queueName    the name of the queue
     * @param exchangeName the exchange name
     * @param routingKey   the routing key
     */
    private void createTopicExchangeQueue(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        admin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        admin.declareExchange(exchange);
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);
        admin.declareBinding(binding);
    }
}
