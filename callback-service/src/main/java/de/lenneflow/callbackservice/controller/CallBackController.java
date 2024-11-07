package de.lenneflow.callbackservice.controller;

import de.lenneflow.callbackservice.component.QueueController;
import de.lenneflow.callbackservice.dto.ResultQueueElement;
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


    @PostMapping("/{step-instance-id}/{workflow-instance-id}")
    public void workerCallBack(@RequestBody FunctionPayload payload, @PathVariable("step-instance-id") String stepInstanceId, @PathVariable("workflow-instance-id") String workflowInstanceId){
        Validator.validate(payload);
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setStepInstanceId(stepInstanceId);
        resultQueueElement.setWorkflowInstanceId(workflowInstanceId);
        resultQueueElement.setOutputData(payload.getOutputData());
        resultQueueElement.setRunStatus(payload.getRunStatus());
        resultQueueElement.setInputData(payload.getInputData());
        resultQueueElement.setCallBackUrl(payload.getCallBackUrl());
        resultQueueElement.setFailureReason(payload.getFailureReason());
        new Thread(() ->queueController.addFunctionDtoToResultQueue(resultQueueElement)).start();
    }
}
