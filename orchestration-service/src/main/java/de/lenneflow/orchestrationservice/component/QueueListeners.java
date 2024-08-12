package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.configuration.AppConfiguration;
import de.lenneflow.orchestrationservice.feignmodels.Function;
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
        Function function = Util.deserializeFunction(serializedFunction);
        new Thread(() -> functionService.processFunctionFromQueue(function)).start();
    }

    @RabbitListener(queues = AppConfiguration.FUNCTIONRESULTQUEUE)
    public void functionResultListener(byte[] serializedFunction) {
        Function resultFunction = Util.deserializeFunction(serializedFunction);
        workflowRunner.processFunctionResultFromQueue(resultFunction);
    }
}
