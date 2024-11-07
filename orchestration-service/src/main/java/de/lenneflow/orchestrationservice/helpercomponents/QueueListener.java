package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener for all queues events.
 *
 * @author Idrissa Ganemtore
 */
@Component
@EnableRabbit
public class QueueListener {

    final AmqpAdmin admin;
    final WorkflowRunner workflowRunner;


    public QueueListener(AmqpAdmin admin, WorkflowRunner workflowRunner) {
        this.admin = admin;
        this.workflowRunner = workflowRunner;
    }

    /**
     * Listener for the function queue. this queue contains the functions from different workflow instances that
     * should be processed.
     *
     * @param serializedElement the serialized function from the queue.
     */
    @RabbitListener(queues = QueueController.FUNCTION_QUEUE)
    public void queueListener(byte[] serializedElement) {
        QueueElement queueElement = Util.deserializeQueueElement(serializedElement);
        new Thread(() -> workflowRunner.processFunctionDtoFromQueue(queueElement)).start();

    }

    /**
     * Listener for the function result queue. this queue contains the functions from the workers that
     * should be processed.
     *
     * @param serializedFunction the serialized function from the queue.
     */
    @RabbitListener(queues = QueueController.FUNCTION_RESULT_QUEUE)
    public void resultQueueListener(byte[] serializedFunction) {
        ResultQueueElement resultQueueElement = Util.deserializeResultQueueElement(serializedFunction);
        workflowRunner.processResultFromQueue(resultQueueElement);
    }
}
