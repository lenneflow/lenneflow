package de.lenneflow.callbackservice.controller;

import de.lenneflow.callbackservice.component.QueueController;
import de.lenneflow.callbackservice.dto.FunctionDTO;
import de.lenneflow.callbackservice.dto.FunctionPayload;
import de.lenneflow.callbackservice.util.Validator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/callback")
public class CallBackController {

    private final QueueController queueController;

    public CallBackController(QueueController queueController) {
        this.queueController = queueController;
    }


    @PostMapping("/{execution-id}/{step-instance-id}/{workflow-instance-id}")
    public void workerCallBack(@RequestBody FunctionPayload payload, @PathVariable("execution-id") String executionId, @PathVariable("step-instance-id") String stepInstanceId, @PathVariable("workflow-instance-id") String workflowInstanceId){
        Validator.validate(payload);
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setExecutionId(executionId);
        functionDTO.setStepInstanceId(stepInstanceId);
        functionDTO.setWorkflowInstanceId(workflowInstanceId);
        functionDTO.setOutputData(payload.getOutputData());
        functionDTO.setRunStatus(payload.getRunStatus());
        functionDTO.setInputData(payload.getInputData());
        functionDTO.setCallBackUrl(payload.getCallBackUrl());
        functionDTO.setFailureReason(payload.getFailureReason());
        new Thread(() ->queueController.addFunctionDtoToResultQueue(functionDTO)).start();
    }
}
