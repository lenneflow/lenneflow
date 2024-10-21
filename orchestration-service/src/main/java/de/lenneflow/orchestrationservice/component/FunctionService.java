package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FunctionService {

    private final RestTemplate restTemplate;
    private final QueueController queueController;

    public FunctionService(RestTemplate restTemplate, QueueController queueController) {
        this.restTemplate = restTemplate;
        this.queueController = queueController;
    }

    public void processFunctionDtoFromQueue(FunctionDto functionDto) {
        Map<String, Object> inputData = functionDto.getInputData();
        String serviceUrl = functionDto.getServiceUrl();
        ResponseEntity<Void> response =  restTemplate.exchange(serviceUrl, HttpMethod.POST, new HttpEntity<>(inputData), Void.class);
        if (response.getStatusCode().value() != 200) {
            functionDto.setRunStatus(RunStatus.CANCELED);
            functionDto.setFailureReason("Could not send request to the cluster!");
            queueController.addFunctionDtoToResultQueue(functionDto);
        }
    }


}
