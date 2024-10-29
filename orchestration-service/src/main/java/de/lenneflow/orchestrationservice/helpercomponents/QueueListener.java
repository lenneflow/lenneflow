package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.configuration.AppConfiguration;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
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
     * @param serializedFunction the serialized function from the queue.
     */
    @RabbitListener(queues = AppConfiguration.FUNCTIONQUEUE)
    public void functionListener(byte[] serializedFunction) {
        FunctionDto functionDto = Util.deserializeFunction(serializedFunction);
        new Thread(() -> workflowRunner.processFunctionDtoFromQueue(functionDto)).start();

    }

    /**
     * Listener for the function result queue. this queue contains the functions from the workers that
     * should be processed.
     *
     * @param serializedFunction the serialized function from the queue.
     */
    @RabbitListener(queues = AppConfiguration.FUNCTIONRESULTQUEUE)
    public void functionResultListener(byte[] serializedFunction) {
        FunctionDto resultFunctionDto = Util.deserializeFunction(serializedFunction);
        workflowRunner.processResultFromQueue(resultFunctionDto);
    }
}
