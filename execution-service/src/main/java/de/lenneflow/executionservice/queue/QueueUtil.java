package de.lenneflow.executionservice.queue;

import de.lenneflow.executionservice.enums.RunNode;
import de.lenneflow.executionservice.feignmodels.Task;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueUtil {

    private final AmqpAdmin admin;
    private final RabbitTemplate rabbitTemplate;

    public QueueUtil(AmqpAdmin admin, RabbitTemplate rabbitTemplate) {
        this.admin = admin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void addTaskToQueue(Task task) {
        if(task.getRunNode() == RunNode.SYSTEM){
            addSystemTaskToQueue(task);
        }else{
            addWorkerTaskToQueue(task);
        }
    }

    private void addWorkerTaskToQueue(Task task) {
        String queueName = task.getTaskType();
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, task);
    }

    private void addSystemTaskToQueue(Task task) {
        String queueName = "SystemQueue";
        String exchange  = queueName + "-Exchange";
        String routingKey = queueName + "-RoutingKey";
        createQueueAndBinding(queueName, exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, task);
    }

    public void createQueueAndBinding(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        admin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        admin.declareExchange(exchange);
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);
        admin.declareBinding(binding);
    }
}
