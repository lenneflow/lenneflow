package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.configuration.AppConfiguration;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class QueueListeners {

    private final WorkflowRunner workflowRunner;
    private final FunctionService functionService;

    public QueueListeners(WorkflowRunner workflowRunner, FunctionService functionService) {
        this.workflowRunner = workflowRunner;
        this.functionService = functionService;
    }

    @RabbitListener(queues = AppConfiguration.FUNCTIONQUEUE)
    public void functionListener(byte[] serializedFunction) {
        FunctionDto functionDto = Util.deserializeFunction(serializedFunction);
        new Thread(() -> functionService.processFunctionDtoFromQueue(functionDto)).start();

    }

    @RabbitListener(queues = AppConfiguration.FUNCTIONRESULTQUEUE)
    public void functionResultListener(byte[] serializedFunction) {
        FunctionDto resultFunctionDto = Util.deserializeFunction(serializedFunction);
        workflowRunner.processResultFromQueue(resultFunctionDto);
    }
}
