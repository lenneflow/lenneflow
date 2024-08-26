package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.configuration.AppConfiguration;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@EnableRabbit
public class QueueListeners {

    private final WorkflowRunner workflowRunner;
    private final FunctionService functionService;

    public QueueListeners(AmqpAdmin admin, RabbitTemplate rabbitTemplate, RestTemplate restTemplate, WorkflowRunner workflowRunner, FunctionService functionService) {
        this.workflowRunner = workflowRunner;
        this.functionService = functionService;
    }

    @RabbitListener(queues = AppConfiguration.FUNCTIONQUEUE)
    public void functionListener(byte[] serializedFunction) {
        FunctionDto functionDto = Util.deserializeFunction(serializedFunction);
        //TODO thread should be replaced by Async
        new Thread(() -> {functionService.processFunctionDtoFromQueue(functionDto);}).start();

    }

    @RabbitListener(queues = AppConfiguration.FUNCTIONRESULTQUEUE)
    public void functionResultListener(byte[] serializedFunction) {
        FunctionDto resultFunctionDto = Util.deserializeFunction(serializedFunction);
        workflowRunner.processResultFromQueue(resultFunctionDto);
    }
}
