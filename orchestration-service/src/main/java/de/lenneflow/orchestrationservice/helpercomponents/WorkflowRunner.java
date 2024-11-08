package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.FunctionPayload;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
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
import de.lenneflow.orchestrationservice.dto.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ExpressionEvaluator;
import de.lenneflow.orchestrationservice.utils.ObjectMapper;
import de.lenneflow.orchestrationservice.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRunner.class);

    private static final String FUNCTION_OR_SUB_WORKFLOW_NOT_FOUND = "Could not find function or sub workflow to execute";

    @Value("${qms.api.root.link}")  private String callBackRoot;

    final FunctionServiceClient functionServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;
    final InstanceController instanceController;
    final ExpressionEvaluator expressionEvaluator;
    final RestTemplate restTemplate;

    WorkflowRunner(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, InstanceController instanceController, ExpressionEvaluator expressionEvaluator, RestTemplate restTemplate) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
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
     * @param workflowInstance      The workflow instance.
     * @return a workflow execution object.
     */
    public WorkflowExecution startWorkflow(WorkflowInstance workflowInstance) {

        instanceController.setStartTime(workflowInstance);

        WorkflowStepInstance firstStepInstance = instanceController.getStartStep(workflowInstance);
        if(firstStepInstance == null){
            throw new InternalServiceException("Could not find start step");
        }
        if(firstStepInstance.getControlStructure() == ControlStructure.SUB_WORKFLOW) {
            runStep(firstStepInstance, null);
            return new WorkflowExecution(workflowInstance);
        }
        Function function = functionServiceClient.getFunctionByUid(firstStepInstance.getFunctionId());
        QueueElement queueElement = generateRunQueueElement(workflowInstance, firstStepInstance, function);

        List<Function> undeployedFunctions = getUndeployedFunctions(workflowInstance);
        if(undeployedFunctions.isEmpty()){
            instanceController.updateRunStatus(workflowInstance, RunStatus.RUNNING);
            runStep(firstStepInstance, queueElement);
        }else{
            instanceController.updateRunStatus(workflowInstance, RunStatus.DEPLOYING_FUNCTIONS);
            new Thread(() -> deployFunctionsFirstAndRunStep(undeployedFunctions, workflowInstance, queueElement, firstStepInstance)).start();
        }
        return new WorkflowExecution(workflowInstance);
    }

    /**
     * Method that stops the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowInstanceId The workflow instance ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution stopWorkflow(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        instanceController.updateRunStatus(workflowInstance, RunStatus.STOPPED);
        return new WorkflowExecution(workflowInstance);
    }

    /**
     * Method that pauses the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowInstanceId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution pauseWorkflow(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        instanceController.updateRunStatus(workflowInstance, RunStatus.PAUSED);
        return new WorkflowExecution(workflowInstance);
    }

    /**
     * Method that resumes the workflow execution by updating the workflow instance and the workflow execution instance
     * and then running the next step of the workflow.
     *
     * @param workflowInstanceId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution resumeWorkflow(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        instanceController.updateRunStatus(workflowInstance, RunStatus.RUNNING);
        return new WorkflowExecution(workflowInstance);
    }

    /**
     * If called, this method will return the current state of the workflow execution.
     *
     * @param workflowInstanceId The workflow execution ID
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution getCurrentExecutionState(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        return new WorkflowExecution(workflowInstance);
    }

    /**
     * Process a function object to send and add it to the send queue.
     *
     * @param queueElement the function dto object
     */
    public void processFunctionDtoFromQueue(QueueElement queueElement) {
        logger.info("Start processing function {} from send queue.", queueElement.getFunctionName());
        Map<String, Object> inputData = queueElement.getInputData();
        String serviceUrl = queueElement.getServiceUrl();
        String callBackUrl = callBackRoot + "/" + queueElement.getStepInstanceId() + "/" + queueElement.getWorkflowInstanceId();

        FunctionPayload functionPayload = new FunctionPayload();
        functionPayload.setInputData(inputData);
        functionPayload.setCallBackUrl(callBackUrl);
        functionPayload.setFailureReason("");

        logger.info("Send function {} with a post request to the url {}", queueElement.getFunctionName(), serviceUrl);
        ResponseEntity<Void> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, new HttpEntity<>(functionPayload), Void.class);
        if (response.getStatusCode().value() != 200) {
            logger.error("send request to the url {} failed.", serviceUrl);
            ResultQueueElement resultQueueElement = new ResultQueueElement();
            resultQueueElement.setWorkflowInstanceId(queueElement.getWorkflowInstanceId());
            resultQueueElement.setStepInstanceId(queueElement.getStepInstanceId());
            resultQueueElement.setRunStatus(RunStatus.CANCELED);
            resultQueueElement.setFailureReason("Could not send request to the cluster!");
            //in case of send failure, the dto is added directly to the result queue with the run status cancelled.
            queueController.addElementToResultQueue(resultQueueElement);
        }
    }

    /**
     * Process a function object that is in the result queue
     *
     * @param resultQueueElement function object
     */
    public void processResultFromQueue(ResultQueueElement resultQueueElement) {
        logger.info("Start processing Queue element with the state {} from the results queue.", resultQueueElement.getRunStatus());
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(resultQueueElement.getWorkflowInstanceId());
        WorkflowStepInstance workflowStepInstance = workflowStepInstanceRepository.findByUid(resultQueueElement.getStepInstanceId());

        instanceController.setEndTime(workflowStepInstance);
        instanceController.mapResultToStepInstance(workflowStepInstance, resultQueueElement);

        //Proceed next steps

        //Check if this is the last step and if the next instance is null because if could be a while step
        if (workflowStepInstance.getRunOrderLabel() == RunOrderLabel.LAST && instanceController.getNextWorkflowStepInstance(workflowStepInstance) == null){
                terminateWorkflowRun(workflowInstance, workflowStepInstance.getRunStatus(), "");
                return;
        }

        switch (resultQueueElement.getRunStatus()) {
            case COMPLETED, SKIPPED:
                processStepCompletedOrSkipped(workflowInstance, workflowStepInstance);
                break;
            case FAILED, TIMED_OUT:
                processStepFailedOrTimedOut(workflowInstance, workflowStepInstance);
                break;
            case CANCELED, FAILED_WITH_TERMINAL_ERROR:
                processStepCancelledOrFailedWithTerminalError(workflowInstance, workflowStepInstance);
                break;
            default:
                logger.error("The state {} is unknown.", resultQueueElement.getRunStatus());
                throw new IllegalStateException("Unexpected value: " + resultQueueElement.getRunStatus());
        }
    }

    private void processStepCompletedOrSkipped(WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        WorkflowStepInstance nextStepInstance = instanceController.getNextWorkflowStepInstance(workflowStepInstance);
        if (nextStepInstance != null) {
            Object object = getElementToExecute(nextStepInstance);
            if (object != null) {
                if(object instanceof Function function){
                    QueueElement queueElement = generateRunQueueElement(workflowInstance, nextStepInstance, function);
                    runStep(nextStepInstance, queueElement);

                }
                if(object instanceof Workflow){
                    runStep(workflowStepInstance, null);
                }

            } else {
                terminateWorkflowRun(workflowInstance, RunStatus.FAILED, FUNCTION_OR_SUB_WORKFLOW_NOT_FOUND);
            }
        }
    }

    /**
     * Method that processes failed execution steps or steps that run in time out.
     *
     * @param workflowInstance     The workflow instance object
     * @param workflowStepInstance The workflow step instance object
     */
    private void processStepFailedOrTimedOut(WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        if (workflowStepInstance.getRetryCount() > 0) {
            workflowStepInstance.setRetryCount(workflowStepInstance.getRetryCount() - 1);
            workflowStepInstanceRepository.save(workflowStepInstance);
            Object object = getElementToExecute(workflowStepInstance);
            if (object != null) {
                if(object instanceof Function function){
                    QueueElement queueElement = generateRunQueueElement(workflowInstance, workflowStepInstance, function);
                    runStep(workflowStepInstance, queueElement);
                }
                else if(object instanceof Workflow){
                    runStep(workflowStepInstance, null);
                }
                else{
                    terminateWorkflowRun(workflowInstance, RunStatus.FAILED, FUNCTION_OR_SUB_WORKFLOW_NOT_FOUND);
                }

            } else {
                terminateWorkflowRun( workflowInstance, RunStatus.FAILED, FUNCTION_OR_SUB_WORKFLOW_NOT_FOUND);
            }
        }
        terminateWorkflowRun(workflowInstance, workflowStepInstance.getRunStatus(), workflowStepInstance.getFailureReason());
    }

    /**
     * Method that processes cancelled execution steps or steps that failed with terminal error.
     *
     * @param workflowInstance     The workflow instance object
     * @param workflowStepInstance The workflow step instance object
     */
    private void processStepCancelledOrFailedWithTerminalError(WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        terminateWorkflowRun( workflowInstance, workflowStepInstance.getRunStatus(), workflowStepInstance.getFailureReason());
    }


    /**
     * Method that terminates a workflow run.
     *
     * @param workflowInstance the running workflow instance
     * @param status           the status to set
     */
    private void terminateWorkflowRun(WorkflowInstance workflowInstance, RunStatus status, String failureReason) {
        instanceController.updateRunStatus(workflowInstance, status);
        instanceController.setEndTime(workflowInstance);
        if (failureReason != null && !failureReason.isEmpty()) {
            instanceController.setFailureReason(workflowInstance, failureReason);
        }
        //If it is a Sub workflow, the parent instance must continue
        if(workflowInstance.getParentInstanceUid() != null && !workflowInstance.getParentInstanceUid().isEmpty()){
            WorkflowInstance parentInstance = workflowInstanceRepository.findByUid(workflowInstance.getParentInstanceUid());
            if(parentInstance != null){
                ResultQueueElement resultQueueElement = new ResultQueueElement();
                resultQueueElement.setWorkflowInstanceId(workflowInstance.getParentInstanceUid());
                resultQueueElement.setStepInstanceId(workflowInstance.getParentStepInstanceUid());
                resultQueueElement.setRunStatus(status);
                resultQueueElement.setFailureReason(failureReason);
                resultQueueElement.setOutputData(workflowInstance.getOutputData());
                queueController.addElementToResultQueue(resultQueueElement);
            }
        }
        instanceController.deleteLastWorkflowInstances(30, 30);
    }


    /**
     * Runs a workflow step by adding it to the queue.
     *
     * @param workflowStepInstance            workflow step to run
     * @param queueElement function to process.
     */
    private void runStep(WorkflowStepInstance workflowStepInstance, QueueElement queueElement) {
        instanceController.setStartTime(workflowStepInstance);
        if(workflowStepInstance.getControlStructure() == ControlStructure.SUB_WORKFLOW){
            Workflow subWorkflow = workflowServiceClient.getWorkflowById(workflowStepInstance.getSubWorkflowId());
            WorkflowInstance subWorkflowInstance = instanceController.generateWorkflowInstance(subWorkflow, workflowStepInstance.getInputData(), workflowStepInstance.getWorkflowUid());
            startWorkflow(subWorkflowInstance);
            return;
        }
        if(queueElement != null){
            Map<String, Object> inputData = workflowStepInstance.getInputData();
            //set values to the input data
            expressionEvaluator.normalizeInputData(workflowStepInstance.getInputData(), workflowStepInstance.getWorkflowInstanceUid());
            queueElement.setInputData(inputData);
            queueController.addFunctionDtoToQueue(queueElement);
            instanceController.updateRunStatus(workflowStepInstance, RunStatus.RUNNING);
        }

    }

    private List<Function> getUndeployedFunctions(WorkflowInstance workflowInstance) {
        List<Function> undeployedFunctions = new ArrayList<>();
        List<WorkflowStepInstance> steps = workflowStepInstanceRepository.findByWorkflowInstanceUid(workflowInstance.getUid());
        for (WorkflowStepInstance step : steps) {
            if(step.getControlStructure() == ControlStructure.SWITCH){
                for(DecisionCase decisionCase : step.getDecisionCases()){
                    Function dcFunction = functionServiceClient.getFunctionByUid(decisionCase.getFunctionUid());
                    if(dcFunction != null && dcFunction.getDeploymentState() != DeploymentState.DEPLOYED){
                        undeployedFunctions.add(dcFunction);
                    }
                }
            }else{
                Function stepFunction = functionServiceClient.getFunctionByUid(step.getFunctionId());
                if(stepFunction != null && stepFunction.getDeploymentState() != DeploymentState.DEPLOYED){
                    undeployedFunctions.add(stepFunction);
                }
            }
        }

        return undeployedFunctions;
    }

    /**
     * In case the lazy deployment flag is true, the function is deployed by runtime.
     * This method search for all undeployed functions in a workflow instance and performs the deployment.
     *
     * @param workflowInstance the workflow instance to run
     */
    private void deployFunctionsFirstAndRunStep(List<Function> undeployedFunctions, WorkflowInstance workflowInstance, QueueElement queueElement, WorkflowStepInstance stepInstance) {
        List<WorkflowStepInstance> steps = workflowStepInstanceRepository.findByWorkflowInstanceUid(workflowInstance.getUid());
        for (Function function : undeployedFunctions) {
            if (function != null && function.isLazyDeployment()  && function.getDeploymentState() == DeploymentState.UNDEPLOYED) {
                functionServiceClient.deployFunction(function.getUid());
            }else if(function != null && !function.isLazyDeployment() && function.getDeploymentState() == DeploymentState.UNDEPLOYED) {
                String reason = "Function " + function.getName() + " is undeployed but the lazy deployment flag is not set!";
                terminateWorkflowRun(workflowInstance, RunStatus.FAILED_WITH_TERMINAL_ERROR, reason);
                throw new InternalServiceException(reason);
            }
        }
        while (true) {
            LocalDateTime start = LocalDateTime.now();
            if (allFunctionsDeployed(steps)) {
                instanceController.updateRunStatus(workflowInstance, RunStatus.RUNNING);
                runStep(stepInstance, queueElement);
                break;
            }
            if (start.plusMinutes(10).isBefore(LocalDateTime.now())) {
                String reason = "All functions could not be deployed in time. Workflow run will be cancelled!";
                terminateWorkflowRun(workflowInstance, RunStatus.FAILED_WITH_TERMINAL_ERROR, reason);
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
     * Method that returns the function to execute from a workflow step instance.
     *
     * @param stepInstance the workflow step instance
     * @return the function to run
     */
    private Object getElementToExecute(WorkflowStepInstance stepInstance) {
        if (Objects.requireNonNull(stepInstance.getControlStructure()) == ControlStructure.SWITCH) {
            String switchCase = expressionEvaluator.evaluateStringExpression(stepInstance.getWorkflowInstanceUid(), stepInstance.getSwitchCase());
            DecisionCase decisionCase = getDecisionCaseByName(stepInstance.getDecisionCases(), switchCase);
            if (decisionCase != null) {
                return getFunctionOrSubworkflow(stepInstance, decisionCase);
            }
            if ((decisionCase = getDecisionCaseByName(stepInstance.getDecisionCases(), "default")) != null) {
                return getFunctionOrSubworkflow(stepInstance, decisionCase);
            }
            return null;
        }
        if (Objects.requireNonNull(stepInstance.getControlStructure()) == ControlStructure.SUB_WORKFLOW) {
            return workflowServiceClient.getWorkflowById(stepInstance.getSubWorkflowId());
        }
        return functionServiceClient.getFunctionByUid(stepInstance.getFunctionId());
    }


    private Object getFunctionOrSubworkflow(WorkflowStepInstance stepInstance, DecisionCase decisionCase) {
        Function func =  functionServiceClient.getFunctionByUid(decisionCase.getFunctionUid());
        stepInstance.setSelectedCaseName(decisionCase.getName());
        if(decisionCase.isSubWorkflow()){
            return workflowServiceClient.getWorkflowById(stepInstance.getSubWorkflowId());
        }
        stepInstance.setInputData(decisionCase.getInputData());
        workflowStepInstanceRepository.save(stepInstance);
        return func;
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

    private QueueElement generateRunQueueElement(WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance, Function function) {
        QueueElement queueElement = ObjectMapper.mapFunctionQueueElement(function);
        queueElement.setStepInstanceId(workflowStepInstance.getUid());
        queueElement.setWorkflowInstanceId(workflowInstance.getUid());
        queueElement.setInputData(workflowStepInstance.getInputData());
        queueElement.setOutputData(workflowStepInstance.getOutputData());
        queueElement.setRunStatus(workflowStepInstance.getRunStatus());
        return queueElement;
    }


}
