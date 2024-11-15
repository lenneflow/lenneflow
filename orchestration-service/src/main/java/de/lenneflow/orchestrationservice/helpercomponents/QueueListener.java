package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.utils.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listener for all queues events.
 *
 * @author Idrissa Ganemtore
 */
@Component
@EnableRabbit
@RequiredArgsConstructor
public class QueueListener {

    private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);

    final AmqpAdmin admin;
    final WorkflowRunner workflowRunner;


    /**
     * Listener for the function queue. this queue contains the functions from different workflow instances that
     * should be processed.
     *
     * @param serializedElement the serialized function from the queue.
     */
    @RabbitListener(queues = QueueController.FUNCTION_QUEUE)
    public void queueListener(byte[] serializedElement) {
        try {
            QueueElement queueElement = Util.deserializeQueueElement(serializedElement);
            new Thread(() -> workflowRunner.processFunctionDtoFromQueue(queueElement)).start();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }


    }

    /**
     * Listener for the function result queue. this queue contains the functions from the workers that
     * should be processed.
     *
     * @param serializedElement the serialized element from the queue.
     */
    @RabbitListener(queues = QueueController.FUNCTION_RESULT_QUEUE)
    public void resultQueueListener(byte[] serializedElement) {
        try {
            ResultQueueElement resultQueueElement = Util.deserializeResultQueueElement(serializedElement);
            workflowRunner.processResultFromQueue(resultQueueElement);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
}
