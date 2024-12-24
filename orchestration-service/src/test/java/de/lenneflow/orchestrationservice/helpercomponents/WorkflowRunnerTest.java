package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.dto.WorkflowExecution;
import de.lenneflow.orchestrationservice.enums.ControlStructure;
import de.lenneflow.orchestrationservice.enums.DeploymentState;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowRunnerTest {

    @Mock
    private FunctionServiceClient functionServiceClient;

    @Mock
    private WorkflowServiceClient workflowServiceClient;

    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Mock
    private WorkflowStepInstanceRepository workflowStepInstanceRepository;

    @Mock
    private QueueController queueController;

    @Mock
    private InstanceController instanceController;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WorkflowRunner workflowRunner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startWorkflow_shouldStartWorkflow() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowStepInstance firstStepInstance = new WorkflowStepInstance();
        firstStepInstance.setControlStructure(ControlStructure.SIMPLE);

        when(instanceController.getStartStep(workflowInstance)).thenReturn(firstStepInstance);
        when(functionServiceClient.getFunctionByUid(any())).thenReturn(new Function());

        WorkflowExecution result = workflowRunner.startWorkflow(workflowInstance);

        assertNotNull(result);
        verify(instanceController).setStartTime(workflowInstance);
    }

    @Test
    void stopWorkflow_shouldStopWorkflow() {
        String workflowInstanceId = "workflowInstanceId";
        WorkflowInstance workflowInstance = new WorkflowInstance();

        when(workflowInstanceRepository.findByUid(workflowInstanceId)).thenReturn(workflowInstance);

        WorkflowExecution result = workflowRunner.stopWorkflow(workflowInstanceId);

        assertNotNull(result);
        verify(instanceController).updateRunStatus(workflowInstance, RunStatus.STOPPED);
    }

    @Test
    void processFunctionDtoFromQueue_shouldProcessFunction() {
        QueueElement queueElement = new QueueElement();
        queueElement.setFunctionName("functionName");
        queueElement.setServiceUrl("serviceUrl");
        queueElement.setStepInstanceId("stepInstanceId");
        queueElement.setWorkflowInstanceId("workflowInstanceId");

        ResponseEntity<Void> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.exchange(eq("serviceUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(responseEntity);

        workflowRunner.processFunctionDtoFromQueue(queueElement);

        verify(restTemplate).exchange(eq("serviceUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void processResultFromQueue_shouldProcessResult() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setWorkflowInstanceId("workflowInstanceId");
        resultQueueElement.setStepInstanceId("stepInstanceId");
        resultQueueElement.setRunStatus(RunStatus.COMPLETED);

        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowStepInstance workflowStepInstance = new WorkflowStepInstance();

        when(workflowInstanceRepository.findByUid("workflowInstanceId")).thenReturn(workflowInstance);
        when(workflowStepInstanceRepository.findByUid("stepInstanceId")).thenReturn(workflowStepInstance);

        workflowRunner.processResultFromQueue(resultQueueElement);

        verify(instanceController).setEndTime(workflowStepInstance);
        verify(instanceController).mapResultToStepInstance(workflowStepInstance, resultQueueElement);
    }

    @Test
    void startWorkflow_shouldThrowExceptionWhenNoStartStep() {
        WorkflowInstance workflowInstance = new WorkflowInstance();

        when(instanceController.getStartStep(workflowInstance)).thenReturn(null);

        assertThrows(InternalServiceException.class, () -> workflowRunner.startWorkflow(workflowInstance));
    }

    @Test
    void startWorkflow_shouldDeployFunctionsFirst() {

        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setUid("workflowInstanceId");

        WorkflowStepInstance firstStepInstance = new WorkflowStepInstance();
        firstStepInstance.setWorkflowInstanceUid("workflowInstanceId");
        firstStepInstance.setControlStructure(ControlStructure.SIMPLE);

        Function undeployedFunction = new Function();
        undeployedFunction.setUid("function");
        undeployedFunction.setDeploymentState(DeploymentState.UNDEPLOYED);
        undeployedFunction.setLazyDeployment(true);

        List<WorkflowStepInstance> stepList = new ArrayList<>();
        stepList.add(firstStepInstance);

        when(instanceController.getStartStep(workflowInstance)).thenReturn(firstStepInstance);
        when(workflowStepInstanceRepository.findByWorkflowInstanceUid(any())).thenReturn(stepList);
        when(functionServiceClient.getFunctionByUid(any())).thenReturn(new Function(), undeployedFunction);

        WorkflowExecution result = workflowRunner.startWorkflow(workflowInstance);

        assertNotNull(result);
        verify(instanceController).updateRunStatus(workflowInstance, RunStatus.DEPLOYING_FUNCTIONS);
    }

    @Test
    void processFunctionDtoFromQueue_shouldHandleFailedRequest() {
        QueueElement queueElement = new QueueElement();
        queueElement.setFunctionName("functionName");
        queueElement.setServiceUrl("serviceUrl");
        queueElement.setStepInstanceId("stepInstanceId");
        queueElement.setWorkflowInstanceId("workflowInstanceId");

        ResponseEntity<Void> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(eq("serviceUrl"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(responseEntity);

        workflowRunner.processFunctionDtoFromQueue(queueElement);

        verify(queueController).addElementToResultQueue(any(ResultQueueElement.class));
    }

    @Test
    void processResultFromQueue_shouldTerminateWorkflowRun() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setWorkflowInstanceId("workflowInstanceId");
        resultQueueElement.setStepInstanceId("stepInstanceId");
        resultQueueElement.setRunStatus(RunStatus.COMPLETED);
        resultQueueElement.setOutputData(new HashMap<>());

        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowStepInstance workflowStepInstance = new WorkflowStepInstance();
        workflowStepInstance.setRunOrderLabel(RunOrderLabel.LAST);

        when(workflowInstanceRepository.findByUid("workflowInstanceId")).thenReturn(workflowInstance);
        when(workflowStepInstanceRepository.findByUid("stepInstanceId")).thenReturn(workflowStepInstance);
        when(instanceController.getNextWorkflowStepInstance(workflowStepInstance)).thenReturn(null);
        doNothing().when(instanceController).deleteLastWorkflowInstances(anyInt(), anyInt());

        workflowRunner.processResultFromQueue(resultQueueElement);

         verify(instanceController).deleteLastWorkflowInstances(100, 100);
    }

    @Test
    void pauseWorkflow_shouldPauseWorkflow() {
        String workflowInstanceId = "workflowInstanceId";
        WorkflowInstance workflowInstance = new WorkflowInstance();

        when(workflowInstanceRepository.findByUid(workflowInstanceId)).thenReturn(workflowInstance);

        WorkflowExecution result = workflowRunner.pauseWorkflow(workflowInstanceId);

        assertNotNull(result);
        verify(instanceController).updateRunStatus(workflowInstance, RunStatus.PAUSED);
    }

    @Test
    void resumeWorkflow_shouldResumeWorkflow() {
        String workflowInstanceId = "workflowInstanceId";
        WorkflowInstance workflowInstance = new WorkflowInstance();

        when(workflowInstanceRepository.findByUid(workflowInstanceId)).thenReturn(workflowInstance);

        WorkflowExecution result = workflowRunner.resumeWorkflow(workflowInstanceId);

        assertNotNull(result);
        verify(instanceController).updateRunStatus(workflowInstance, RunStatus.RUNNING);
    }

    @Test
    void getCurrentExecutionState_shouldReturnCurrentState() {
        String workflowInstanceId = "workflowInstanceId";
        WorkflowInstance workflowInstance = new WorkflowInstance();

        when(workflowInstanceRepository.findByUid(workflowInstanceId)).thenReturn(workflowInstance);

        WorkflowExecution result = workflowRunner.getCurrentExecutionState(workflowInstanceId);

        assertNotNull(result);
        assertEquals(workflowInstance.getName(), result.getWorkflowName());
    }
}