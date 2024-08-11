package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.OrchestrationServiceApplication;
import de.lenneflow.orchestrationservice.enums.FunctionStatus;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.utils.Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Configuration
@EnableRabbit
public class FunctionProcessor{

    private final RestTemplate restTemplate;
    private final QueueController queueController;

    public FunctionProcessor(RestTemplate restTemplate, QueueController queueController) {
        this.restTemplate = restTemplate;
        this.queueController = queueController;
    }

    @RabbitListener(queues = OrchestrationServiceApplication.FUNCTIONQUEUE)
    public void functionListener(byte[] serializedFunction) {
        Function function = Util.deserializeFunction(serializedFunction);
        Map<String, Object> inputData = function.getInputData();
        String endpoint = StringUtils.removeEnd(function.getEndPointRoot(), "/") + "/" + StringUtils.removeStart(function.getEndPointPath(), "/");
        Map<String, Object> outputData = restTemplate.postForObject(endpoint, inputData, Map.class);
        if (outputData != null) {
            function.setOutputData(outputData);
            function.setFunctionStatus(FunctionStatus.COMPLETED);
        }else {
            function.setFunctionStatus(FunctionStatus.FAILED);
        }
        queueController.addFunctionToResultQueue(function);
    }
}
