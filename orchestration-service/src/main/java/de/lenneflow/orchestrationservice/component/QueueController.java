package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.configuration.AppConfiguration;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class QueueController {

    private final AmqpAdmin admin;
    private final RabbitTemplate rabbitTemplate;


    public QueueController(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void addFunctionDtoToQueue(FunctionDto functionDto)  {
        byte[] serializedFunctionDto = Util.serializeFunctionDto(functionDto);
        String queueName = AppConfiguration.FUNCTIONQUEUE;
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
    }

    public void addFunctionDtoToResultQueue(FunctionDto functionDto)  {
        byte[] serializedFunctionDto = Util.serializeFunctionDto(functionDto);
        String queueName = AppConfiguration.FUNCTIONRESULTQUEUE;
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunctionDto);
    }

    private void createQueueAndBinding(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        admin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        admin.declareExchange(exchange);
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);
        admin.declareBinding(binding);
    }
}
