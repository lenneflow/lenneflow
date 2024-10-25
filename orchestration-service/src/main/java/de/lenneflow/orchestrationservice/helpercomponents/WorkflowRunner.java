package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.ControlStructure;
import de.lenneflow.orchestrationservice.enums.DeploymentState;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is the controller for all workflow runs.
 *
 * @author Idrissa Ganemtore
 */
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
    final RestTemplate restTemplate;

    WorkflowRunner(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, InstanceController instanceController, ExpressionEvaluator expressionEvaluator, RestTemplate restTemplate) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
        this.instanceController = instanceController;
        this.expressionEvaluator = expressionEvaluator;
        this.restTemplate = restTemplate;
    }

    /**
     * This is the start method of every workflow run. This method searches for the starting workflow step, gets the function
     * associated to the step and run the workflow step.
     *
     * @param workflowId      The Name od the workflow to run.
     * @param inputParameters The input parameters for the workflow run.
     * @return a workflow execution object.
     */
    public WorkflowExecution startWorkflow(String workflowId, Map<String, Object> inputParameters) {
        WorkflowInstance workflowInstance = instanceController.createWorkflowInstance(workflowId, inputParameters);
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        WorkflowExecution execution = createWorkflowExecution(workflow, workflowInstance);

        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.DEPLOYING_FUNCTIONS);
        checkAndWaitUntilFullDeployment(workflowInstance, execution, 10);

        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.RUNNING);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.STOPPED);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.PAUSED);
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
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, RunStatus.RUNNING);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * If called, this method will return the current state of the workflow execution.
     *
     * @param workflowExecutionId The workflow execution ID
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution getCurrentExecutionState(String workflowExecutionId) {
        return workflowExecutionRepository.findByRunId(workflowExecutionId);
    }


    /**
     * Process a function object that is in the result queue
     *
     * @param resultFunctionDto function object
     */
    public void processResultFromQueue(FunctionDto resultFunctionDto) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(resultFunctionDto.getExecutionId());
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(resultFunctionDto.getWorkflowInstanceId());
        WorkflowStepInstance workflowStepInstance = workflowStepInstanceRepository.findByUid(resultFunctionDto.getStepInstanceId());

        instanceController.updateWorkflowStepInstance(workflowStepInstance, resultFunctionDto);

        if (workflowStepInstance.getRunOrderLabel() == RunOrderLabel.LAST) {
            terminateWorkflowRun(execution, workflowInstance, workflowStepInstance.getRunStatus(), null);
            return;
        }
        switch (resultFunctionDto.getRunStatus()) {
            case COMPLETED, SKIPPED:
                processStepCompletedOrSkipped(execution, workflowInstance, workflowStepInstance);
                break;
            case FAILED, TIMED_OUT:
                processStepFailedOrTimedOut(execution, workflowInstance, workflowStepInstance);
                break;
            case CANCELED, FAILED_WITH_TERMINAL_ERROR:
                processStepCancelledOrFailedWithTerminalError(execution, workflowInstance, workflowStepInstance);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + resultFunctionDto.getRunStatus());
        }
    }

    /**
     * Process a function object to send and add it to the send queue.
     *
     * @param functionDto the function dto object
     */
    public void processFunctionDtoFromQueue(FunctionDto functionDto) {
        Map<String, Object> inputData = functionDto.getInputData();
        String serviceUrl = functionDto.getServiceUrl();
        ResponseEntity<Void> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, new HttpEntity<>(inputData), Void.class);
        if (response.getStatusCode().value() != 200) {
            functionDto.setRunStatus(RunStatus.CANCELED);
            functionDto.setFailureReason("Could not send request to the cluster!");
            //in case of send failure, the dto is added directly to the result queue with the run status cancelled.
            queueController.addFunctionDtoToResultQueue(functionDto);
        }
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
     * @param step        workflow step to run
     */
    private void runStep(FunctionDto functionDto, WorkflowStepInstance step) {
        Map<String, Object> inputData = step.getInputData();
        //set values to the input data
        expressionEvaluator.normalizeInputData(step.getInputData(), step.getWorkflowInstanceUid());
        functionDto.setInputData(inputData);
        queueController.addFunctionDtoToQueue(functionDto);
        instanceController.updateWorkflowStepInstanceStatus(step, RunStatus.RUNNING);
    }

    private void processStepCompletedOrSkipped(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        WorkflowStepInstance nextStepInstance = instanceController.getNextWorkflowStepInstance(workflowStepInstance);
        if (nextStepInstance != null) {
            Function function = getFunctionToExecute(nextStepInstance);
            if (function != null) {
                function.setStepInstanceId(nextStepInstance.getUid());
                function.setExecutionId(execution.getRunId());
                function.setWorkflowInstanceId(workflowInstance.getUid());
                runStep(Util.mapFunctionToDto(function), nextStepInstance);
            } else {
                terminateWorkflowRun(execution, workflowInstance, RunStatus.FAILED, "Could not find function to execute");
            }
        }
    }


    /**
     * In case the lazy deployment flag is true, the function is deployed by runtime.
     * This method search for all undeployed functions in a workflow instance and performs the deployment.
     *
     * @param workflowInstance the workflow instance to run
     */
    private void checkAndWaitUntilFullDeployment(WorkflowInstance workflowInstance, WorkflowExecution execution, int minutesToWait) {
        List<WorkflowStepInstance> steps = workflowStepInstanceRepository.findByWorkflowInstanceUid(workflowInstance.getUid());
        for (WorkflowStepInstance step : steps) {
            Function function = functionServiceClient.getFunctionByUid(step.getFunctionId());
            if (function != null && function.isLazyDeployment()  && function.getDeploymentState() == DeploymentState.UNDEPLOYED) {
                functionServiceClient.deployFunction(step.getFunctionId());
            }else if(function != null && !function.isLazyDeployment() && function.getDeploymentState() == DeploymentState.UNDEPLOYED) {
                String reason = "Function " + function.getName() + " is undeployed but the lazy deployment flag is not set!";
                terminateWorkflowRun(execution, workflowInstance, RunStatus.FAILED_WITH_TERMINAL_ERROR, reason);
                throw new InternalServiceException(reason);
            }
        }
        while (true) {
            LocalDateTime start = LocalDateTime.now();
            if (allFunctionsDeployed(steps)) {
                break;
            }
            if (start.plusMinutes(minutesToWait).isBefore(LocalDateTime.now())) {
                String reason = "All functions could not be deployed in time. Workflow run will be cancelled!";
                terminateWorkflowRun(execution, workflowInstance, RunStatus.FAILED_WITH_TERMINAL_ERROR, reason);
                throw new InternalServiceException(reason);
            }
            Util.pause(5000);

        }
    }

    /**
     * Loop over all steps and check if all functions are deployed
     * @param steps the workflow steps
     * @return the result
     */
    private boolean allFunctionsDeployed(List<WorkflowStepInstance> steps){
        for (WorkflowStepInstance step : steps) {
            Function function = functionServiceClient.getFunctionByUid(step.getFunctionId());
            if (function != null && function.getDeploymentState() == DeploymentState.DEPLOYING) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method that processes failed execution steps or steps that run in time out.
     *
     * @param execution            The workflow execution object
     * @param workflowInstance     The workflow instance object
     * @param workflowStepInstance The workflow step instance object
     */
    private void processStepFailedOrTimedOut(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        if (workflowStepInstance.getRetryCount() > 0) {
            workflowStepInstance.setRetryCount(workflowStepInstance.getRetryCount() - 1);
            workflowStepInstanceRepository.save(workflowStepInstance);
            Function function = getFunctionToExecute(workflowStepInstance);
            if (function != null) {
                runStep(Util.mapFunctionToDto(function), workflowStepInstance);
            } else {
                terminateWorkflowRun(execution, workflowInstance, RunStatus.FAILED, "Could not find function to execute");
            }
        }
        terminateWorkflowRun(execution, workflowInstance, workflowStepInstance.getRunStatus(), workflowStepInstance.getFailureReason());
    }

    /**
     * Method that processes cancelled execution steps or steps that failed with terminal error.
     *
     * @param execution            The workflow execution object
     * @param workflowInstance     The workflow instance object
     * @param workflowStepInstance The workflow step instance object
     */
    private void processStepCancelledOrFailedWithTerminalError(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        terminateWorkflowRun(execution, workflowInstance, workflowStepInstance.getRunStatus(), workflowStepInstance.getFailureReason());
    }


    /**
     * Method that terminates a workflow run. It will go throw all steps status and update the workflow instance status.
     *
     * @param execution        The workflow execution
     * @param workflowInstance the running workflow instance
     * @param status           the status to set
     */
    private void terminateWorkflowRun(WorkflowExecution execution, WorkflowInstance workflowInstance, RunStatus status, String failureReason) {
        instanceController.updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, status);
        instanceController.setWorkflowRunEndTime(workflowInstance, execution);
        if (failureReason != null && !failureReason.isEmpty()) {
            instanceController.setFailureReason(workflowInstance, execution, failureReason);
        }

        instanceController.deleteLastWorkflowInstances(30, 30);
    }


    /**
     * Method that returns the function to execute from a workflow step instance.
     *
     * @param stepInstance the workflow step instance
     * @return the function to run
     */
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


    /**
     * Iterate over a list of decision case objects and find the specified one.
     *
     * @param decisionCaseList the list
     * @param decisionCaseName the decision case to find
     * @return the found decision case object
     */
    private DecisionCase getDecisionCaseByName(List<DecisionCase> decisionCaseList, String decisionCaseName) {
        for (DecisionCase decisionCase : decisionCaseList) {
            if (decisionCase.getName().equals(decisionCaseName)) {
                return decisionCase;
            }
        }
        return null;
    }


}
