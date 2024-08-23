package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FunctionService {

    private final RestTemplate restTemplate;
    private final QueueController queueController;

    public FunctionService(RestTemplate restTemplate, QueueController queueController) {
        this.restTemplate = restTemplate;
        this.queueController = queueController;
    }

    @Async
    public CompletableFuture<Void> processFunctionFromQueue(Function function) {
        //Map<String, Object> inputData = function.getInputData(); //TODO
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("processTimeInMillis", 10000);
        String endpoint = Util.getFunctionEndpointUrl(function);
        Map<String, Object> outputData = restTemplate.postForObject(endpoint, inputData, Map.class);
        if (outputData != null) {
            function.setOutputData(outputData);
            function.setRunStatus(RunStatus.COMPLETED);
        } else {
            function.setRunStatus(RunStatus.FAILED);
        }
        queueController.addFunctionToResultQueue(function);
        return CompletableFuture.completedFuture(null);
    }


}
