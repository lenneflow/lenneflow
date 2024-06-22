package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignmodels.Task;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueController {

    private final AmqpAdmin admin;
    private final RabbitTemplate rabbitTemplate;

    public QueueController(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void addWorkerTaskToQueue(Task task)  {
        String serializedTask = Util.serializeTask(task);
        String queueName = task.getTaskType();
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedTask);
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
