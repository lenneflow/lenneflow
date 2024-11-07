package de.lenneflow.callbackservice.component;

import de.lenneflow.callbackservice.config.AppConfiguration;
import de.lenneflow.callbackservice.dto.ResultQueueElement;
import de.lenneflow.callbackservice.util.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
@EnableAsync
public class QueueController {

    private final AmqpAdmin admin;
    private final RabbitTemplate rabbitTemplate;

    public QueueController(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void addFunctionDtoToResultQueue(ResultQueueElement functionDto)  {
        byte[] serializedFunctionDto = Util.serializeFunctionDto(functionDto);
        String queueName = AppConfiguration.RESULTSQUEUENAME;
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
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
