package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.dto.RunStateDto;
import de.lenneflow.orchestrationservice.utils.Util;
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
public class QueueController {

    public static final String FUNCTION_RESULT_QUEUE = "functionResultQueue";
    public static final String FUNCTION_QUEUE = "functionQueue";
    public static final String RUN_STATE_QUEUE = "runStateQueue";
    public static final String RUN_STATE_EXCHANGE = "runStateExchange";
    public static final String RUN_STATE_ROUTING = "runStateRouting";

    final AmqpAdmin admin;
    final RabbitTemplate rabbitTemplate;


    public QueueController(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishRunStateChange(RunStateDto runStateDto) {
        createFanoutExchangeQueue();
        rabbitTemplate.convertAndSend(RUN_STATE_QUEUE, RUN_STATE_ROUTING, runStateDto);
    }

    /**
     * Adds a function object to the function queue.
     *
     * @param functionDto the function object
     */
    public void addFunctionDtoToQueue(FunctionDto functionDto) {
        byte[] serializedFunctionDto = Util.serializeFunctionDto(functionDto);
        String queueName = FUNCTION_QUEUE;
        String exchange = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createTopicExchangeQueue(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
    }

    /**
     * Adds a function object to the function results queue.
     *
     * @param functionDto the function object
     */
    public void addFunctionDtoToResultQueue(FunctionDto functionDto) {
        byte[] serializedFunctionDto = Util.serializeFunctionDto(functionDto);
        String queueName = FUNCTION_RESULT_QUEUE;
        String exchange = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createTopicExchangeQueue(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
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

    /**
     * Create a rabbitmq queue and the corresponding binding.
     */
    private void createFanoutExchangeQueue() {
        Queue queue = new Queue(QueueController.RUN_STATE_QUEUE, true, false, false);
        admin.declareQueue(queue);
        FanoutExchange exchange = new FanoutExchange(QueueController.RUN_STATE_EXCHANGE, true, false);
        admin.declareExchange(exchange);
        Binding binding = new Binding(QueueController.RUN_STATE_QUEUE, Binding.DestinationType.QUEUE, QueueController.RUN_STATE_EXCHANGE, QueueController.RUN_STATE_ROUTING, null);
        admin.declareBinding(binding);
    }
}
