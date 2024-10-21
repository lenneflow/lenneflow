package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.ControlStructure;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.DecisionCase;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ExpressionEvaluator;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class WorkflowRunner {

    final FunctionServiceClient functionServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;
    final InstanceController instanceController;
    final ExpressionEvaluator expressionEvaluator;

    WorkflowRunner(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, InstanceController instanceController, ExpressionEvaluator expressionEvaluator) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
        this.instanceController = instanceController;
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Implementation of the Function results listener. This method will listen on the function results queue and process
     * the result function
     *
     * @param resultFunctionDto function object
     */
    public void processResultFromQueue(FunctionDto resultFunctionDto) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(resultFunctionDto.getExecutionId());
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(resultFunctionDto.getWorkflowInstanceId());
        WorkflowStepInstance workflowStepInstance = workflowStepInstanceRepository.findByUid(resultFunctionDto.getStepInstanceId());

        instanceController.updateWorkflowStepInstance(workflowStepInstance, resultFunctionDto);

        if (workflowStepInstance.getRunOrderLabel() == RunOrderLabel.LAST) {
            terminateWorkflowRun(execution, workflowInstance, workflowStepInstance);
            return;
        }
        switch (resultFunctionDto.getRunStatus()) {
            case COMPLETED, SKIPPED:
                processStepCompletedOrSkipped(execution,workflowInstance,workflowStepInstance);
                break;
            case FAILED, TIMED_OUT:
                processStepFailedOrTimedOut(execution,workflowInstance,workflowStepInstance);
                break;
            case CANCELED, FAILED_WITH_TERMINAL_ERROR:
                processStepCancelledOrFailedWithTerminalError(execution,workflowInstance,workflowStepInstance);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + resultFunctionDto.getRunStatus());
        }
    }

    /**
     * This is the start method of every workflow run. This method searches for the starting workflow step, gets the function
     * associated to the step and run the workflow step.
     *
     * @param workflowId    The Name od the workflow to run.
     * @param inputParameters The input parameters for the workflow run.
     * @return a workflow execution object.
     */
    public WorkflowExecution startWorkflow(String workflowId, Map<String, Object> inputParameters) {
        WorkflowInstance workflowInstance = instanceController.createWorkflowInstance(workflowId, inputParameters);
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        WorkflowExecution execution = createWorkflowExecution(workflow, workflowInstance);
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution,  RunStatus.RUNNING);

        WorkflowStepInstance firstStepInstance = instanceController.getStartStep(workflowInstance);
        Function function = functionServiceClient.getFunctionByUid(firstStepInstance.getFunctionId());
        FunctionDto functionDto = Util.mapFunctionToDto(function);
        functionDto.setStepInstanceId(firstStepInstance.getUid());
        functionDto.setExecutionId(execution.getRunId());
        functionDto.setWorkflowInstanceId(workflowInstance.getUid());
        runStep(functionDto, firstStepInstance);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, RunStatus.STOPPED);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, RunStatus.PAUSED);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, RunStatus.RUNNING);
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
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow, workflowInstance);
        return workflowExecutionRepository.save(workflowExecution);
    }

    /**
     * Runs a workflow step by adding it to the queue.
     *
     * @param functionDto function to process.
     * @param step     workflow step to run
     */
    private void runStep(FunctionDto functionDto, WorkflowStepInstance step) {
        Map<String, Object> inputData = step.getInputData();
        //set values to the input data
        expressionEvaluator.normalizeInputData(step.getInputData(), step.getWorkflowInstanceUid());
        functionDto.setInputData(inputData);
        queueController.addFunctionDtoToQueue(functionDto);
        instanceController.updateWorkflowStepInstanceStatus(step, RunStatus.RUNNING);
    }

    private void processStepCompletedOrSkipped(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance){
        WorkflowStepInstance nextStepInstance = instanceController.getNextWorkflowStepInstance(workflowStepInstance);
        if (nextStepInstance != null) {
            Function function = getFunctionToExecute(nextStepInstance);
            if(function != null){
                function.setStepInstanceId(nextStepInstance.getUid());
                function.setExecutionId(execution.getRunId());
                function.setWorkflowInstanceId(workflowInstance.getUid());
                runStep(Util.mapFunctionToDto(function), nextStepInstance);
            }else{
                //TODO
            }
        }
    }

    private void processStepFailedOrTimedOut(WorkflowExecution execution,WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance){
        if (workflowStepInstance.getRetryCount() > 0) {
            workflowStepInstance.setRetryCount(workflowStepInstance.getRetryCount() - 1);
            workflowStepInstanceRepository.save(workflowStepInstance);
            Function function = getFunctionToExecute(workflowStepInstance);
            if(function != null) {
                runStep(Util.mapFunctionToDto(function), workflowStepInstance);
            }else{
                //TODO
            }
        }
        terminateWorkflowRun(execution, workflowInstance, workflowStepInstance);
    }

    private void processStepCancelledOrFailedWithTerminalError(WorkflowExecution execution,WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance){
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
        switch (stepInstance.getRunStatus()) {
            case FAILED, FAILED_WITH_TERMINAL_ERROR:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.FAILED);
                break;
            case COMPLETED:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.COMPLETED);
                break;
            case CANCELED:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.CANCELED);
                break;
            case TIMED_OUT:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.TIMED_OUT);
                break;
            default:
                instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.COMPLETED_WITH_ERRORS);
                break;
        }
        instanceController.setWorkflowRunEndTime(workflowInstance, execution);
        //TODO delete workflow and step instances
    }


    private Function getFunctionToExecute(WorkflowStepInstance stepInstance) {
        if (Objects.requireNonNull(stepInstance.getControlStructure()) == ControlStructure.SWITCH) {
            String switchCase = expressionEvaluator.readDataFromPath(stepInstance.getWorkflowInstanceUid(), stepInstance.getSwitchCase());
            DecisionCase decisionCase = getDecisionCaseByName(stepInstance.getDecisionCases(), switchCase);
            if (decisionCase != null) {
                return functionServiceClient.getFunctionByUid(decisionCase.getFunctionId());
            }
            if ((decisionCase = getDecisionCaseByName(stepInstance.getDecisionCases(), "default")) != null) {
                return functionServiceClient.getFunctionByUid(decisionCase.getFunctionId());
            }
            return null;
        }
        return functionServiceClient.getFunctionByUid(stepInstance.getFunctionId());
    }


    private DecisionCase getDecisionCaseByName(List<DecisionCase> decisionCaseList, String decisionCaseName) {
        for (DecisionCase decisionCase : decisionCaseList) {
            if (decisionCase.getName().equals(decisionCaseName)) {
                return decisionCase;
            }
        }
        return null;
    }


}
