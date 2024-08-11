package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.OrchestrationServiceApplication;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
public class QueueController {

    private final AmqpAdmin admin;
    private final RabbitTemplate rabbitTemplate;

    public QueueController(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void addFunctionToQueue(Function function)  {
        byte[] serializedFunction = Util.serializeFunction(function);
        String queueName = OrchestrationServiceApplication.FUNCTIONQUEUE;
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunction);
    }

    public void addFunctionToResultQueue(Function function)  {
        byte[] serializedFunction = Util.serializeFunction(function);
        String queueName = OrchestrationServiceApplication.FUNCTIONRESULTQUEUE;
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        //TODO add queue to listener
        rabbitTemplate.convertAndSend(exchange, routingKey, serializedFunction);
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
