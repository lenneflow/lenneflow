package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.enums.FunctionStatus;
import de.lenneflow.orchestrationservice.enums.WorkFlowStepType;
import de.lenneflow.orchestrationservice.enums.WorkflowStatus;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowRunner {

    final FunctionServiceClient functionServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;
    final InstanceController instanceController;

    WorkflowRunner(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, InstanceController instanceController, RestTemplate restTemplate) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
        this.instanceController = instanceController;
    }

    /**
     * Implementation of the Function results listener. This method will listen on the function results queue and process
     * the result function
     *
     * @param resultFunction function object
     */
    public void processFunctionResultFromQueue(Function resultFunction) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(resultFunction.getExecutionId());
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(resultFunction.getWorkflowInstanceId());
        WorkflowStepInstance workflowStepInstance = workflowStepInstanceRepository.findByUid(resultFunction.getStepInstanceId());

        instanceController.updateWorkflowStepInstance(workflowStepInstance, resultFunction);

        if (workflowStepInstance.getWorkFlowStepType() == WorkFlowStepType.TERMINATE) {
            terminateWorkflowRun(execution, workflowInstance, workflowStepInstance);
            return;
        }
        switch (resultFunction.getFunctionStatus()) {
            case COMPLETED, SKIPPED:
                processStepCompletedOrSkipped(execution,workflowInstance,workflowStepInstance,resultFunction);
                break;
            case FAILED, TIMED_OUT:
                processStepFailedOrTimedOut(execution,workflowInstance,workflowStepInstance,resultFunction);
                break;
            case CANCELED, FAILED_WITH_TERMINAL_ERROR:
                processStepCancelledOrFailedWithTerminalError(execution,workflowInstance,workflowStepInstance);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + resultFunction.getFunctionStatus());
        }
    }

    private void processStepCompletedOrSkipped(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance, Function resultFunction){
        WorkflowStepInstance nextStepInstance = instanceController.getNextWorkflowStepInstance(workflowStepInstance, resultFunction);
        if (nextStepInstance != null) {
            Function function = functionServiceClient.getFunctionByName(nextStepInstance.getFunctionName());
            function.setStepInstanceId(nextStepInstance.getUid());
            function.setExecutionId(execution.getRunId());
            function.setWorkflowInstanceId(workflowInstance.getUid());
            runStep(function, nextStepInstance);
        }
    }

    private void processStepFailedOrTimedOut(WorkflowExecution execution,WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance, Function resultFunction){
        workflowInstance.setErrorsPresent(true);
        workflowStepInstance.setErrorMessage(resultFunction.getErrorMessage());
        workflowStepInstanceRepository.save(workflowStepInstance);
        if (workflowStepInstance.isRetriable() && workflowStepInstance.getRetryCount() > 0) {
            workflowStepInstance.setRetryCount(workflowStepInstance.getRetryCount() - 1);
            workflowStepInstanceRepository.save(workflowStepInstance);
            Function function = functionServiceClient.getFunctionByName(workflowStepInstance.getFunctionName());
            runStep(function, workflowStepInstance);
        }
        terminateWorkflowRun(execution, workflowInstance, workflowStepInstance);
    }

    private void processStepCancelledOrFailedWithTerminalError(WorkflowExecution execution,WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance){
        workflowInstance.setErrorsPresent(true);
        terminateWorkflowRun(execution, workflowInstance, workflowStepInstance);
    }

    /**
     * Method that terminates a workflow run. It will go throw all steps status and update the workflow instance status.
     *
     * @param execution        The workflow execution
     * @param workflowInstance the running workflow instance
     * @param stepInstance     the last step instance before termination
     */
    private void terminateWorkflowRun(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance stepInstance) {
        if (!workflowInstance.isErrorsPresent()) {
            instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.COMPLETED);
            return;
        }
        Map<String, String> errorMessages = new HashMap<>();
        for (String stepId : workflowInstance.getStepInstanceIds()) {
            WorkflowStepInstance foundStep = workflowStepInstanceRepository.findByUid(stepId);
            if (foundStep != null && foundStep.getErrorMessage() != null && !foundStep.getErrorMessage().isEmpty()) {
                errorMessages.put(foundStep.getStepName(), foundStep.getErrorMessage());
            }
        }
        workflowInstance.setErrorMessages(errorMessages);
        workflowInstanceRepository.save(workflowInstance);
        switch (stepInstance.getFunctionStatus()) {
            case FAILED, FAILED_WITH_TERMINAL_ERROR:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.FAILED);
                break;
            case CANCELED:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.STOPPED);
                break;
            case TIMED_OUT:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.TIMED_OUT);
                break;
            default:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.COMPLETED_WITH_ERRORS);
                break;
        }

        //TODO delete workflow and step instances
    }


    /**
     * This is the start method of every workflow run. This method searches for the starting workflow step, gets the function
     * associated to the step and run the workflow step.
     *
     * @param workflowName    The Name od the workflow to run.
     * @param inputParameters The input parameters for the workflow run.
     * @return a workflow execution object.
     */
    public WorkflowExecution startWorkflow(String workflowName, Map<String, Object> inputParameters) {
        WorkflowInstance workflowInstance = instanceController.newWorkflowInstance(workflowName, inputParameters);
        Workflow workflow = workflowServiceClient.getWorkflowByName(workflowName);
        WorkflowExecution execution = createWorkflowExecution(workflow, workflowInstance);
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.RUNNING);

        WorkflowStepInstance firstStepInstance = instanceController.getStartStep(workflowInstance);
        assert firstStepInstance != null;
        Function function = functionServiceClient.getFunctionByName(firstStepInstance.getFunctionName());
        function.setStepInstanceId(firstStepInstance.getUid());
        function.setExecutionId(execution.getRunId());
        function.setWorkflowInstanceId(workflowInstance.getUid());
        runStep(function, firstStepInstance);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that stops the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution stopWorkflow(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.STOPPED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that pauses the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution pauseWorkflow(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.PAUSED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.PAUSED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that resumes the workflow execution by updating the workflow instance and the workflow execution instance
     * and then running the next step of the workflow.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution resumeWorkflow(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.RUNNING);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * If called, this method will return the current state of the workflow execution.
     *
     * @param workflowExecutionId The workflow execution ID
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution executionState(String workflowExecutionId) {
        return workflowExecutionRepository.findByRunId(workflowExecutionId);
    }


    /**
     * This method creates a workflow execution instance.
     *
     * @param workflow         The workflow object.
     * @param workflowInstance The workflow instance object.
     * @return a workflow execution object
     */
    private WorkflowExecution createWorkflowExecution(Workflow workflow, WorkflowInstance workflowInstance) {
        List<WorkflowStepInstance> stepInstances = new ArrayList<>();
        for (String setInstanceId : workflowInstance.getStepInstanceIds()) {
            stepInstances.add(workflowStepInstanceRepository.findByUid(setInstanceId));
        }
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow, workflowInstance, stepInstances);
        return workflowExecutionRepository.save(workflowExecution);
    }

    /**
     * Runs a workflow step by adding it to the queue.
     *
     * @param function function to process.
     * @param step     workflow step to run
     */
    private void runStep(Function function, WorkflowStepInstance step) {
        function.setInputData(step.getInputData());
        queueController.addFunctionToQueue(function);
        instanceController.updateWorkflowStepInstanceStatus(step, FunctionStatus.IN_PROGRESS);
    }


}
