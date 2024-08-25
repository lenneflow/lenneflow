package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

//    @Async
//    public CompletableFuture<Void> processFunctionDtoFromQueue(FunctionDto functionDto) {
//        Map<String, Object> inputData = functionDto.getInputData();
//        Map<String, Object> outputData = restTemplate.postForObject(functionDto.getServiceUrl(), inputData, Map.class);
//        if (outputData != null) {
//            functionDto.setOutputData(outputData);
//            functionDto.setRunStatus(RunStatus.COMPLETED);
//        } else {
//            functionDto.setRunStatus(RunStatus.FAILED);
//        }
//        queueController.addFunctionDtoToResultQueue(functionDto);
//        return CompletableFuture.completedFuture(null);
//    }

    public void processFunctionDtoFromQueue(FunctionDto functionDto) {
        Map<String, Object> inputData = functionDto.getInputData();
        //TODO Service url to function
        Map<String, Object> outputData = restTemplate.postForObject("http://lenneflowworker/api/functionjava/process", inputData, Map.class);
        if (outputData != null) {
            functionDto.setOutputData(outputData);
            functionDto.setRunStatus(RunStatus.COMPLETED);
        } else {
            functionDto.setRunStatus(RunStatus.FAILED);
        }
        queueController.addFunctionDtoToResultQueue(functionDto);
    }


}
