package de.lenneflow.orchestrationservice.helpercomponents;

import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.enums.ControlStructure;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstanceControllerTest {

    @Mock
    private WorkflowServiceClient workflowServiceClient;

    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Mock
    private WorkflowStepInstanceRepository workflowStepInstanceRepository;

    @Mock
    private QueueController queueController;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    private InstanceController instanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instanceController = new InstanceController(workflowServiceClient, workflowInstanceRepository, workflowStepInstanceRepository, queueController, expressionEvaluator);
    }

    @Test
    void generateWorkflowInstance_createsWorkflowInstanceAndStepInstances() {
        Workflow workflow = new Workflow();
        Map<String, Object> inputData = new HashMap<>();
        String parentInstanceUid = "parentInstanceUid";
        String parentStepInstanceUid = "parentStepInstanceUid";

        when(workflowServiceClient.getStepListByWorkflowId(anyString())).thenReturn(Collections.emptyList());

        WorkflowInstance result = instanceController.generateWorkflowInstance(workflow, inputData, parentInstanceUid, parentStepInstanceUid);

        assertNotNull(result);
        assertEquals(RunStatus.NEW, result.getRunStatus());
        verify(workflowInstanceRepository, times(2)).save(any(WorkflowInstance.class));
    }

    @Test
    void mapResultToStepInstance_updatesWorkflowStepInstance() {
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setRunStatus(RunStatus.COMPLETED);
        resultQueueElement.setOutputData(new HashMap<>());

        instanceController.mapResultToStepInstance(stepInstance, resultQueueElement);

        assertEquals(RunStatus.COMPLETED, stepInstance.getRunStatus());
        verify(workflowStepInstanceRepository).save(stepInstance);
    }

    @Test
    void updateRunStatus_updatesWorkflowInstanceStatus() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        RunStatus runStatus = RunStatus.COMPLETED;

        instanceController.updateRunStatus(workflowInstance, runStatus);

        assertEquals(RunStatus.COMPLETED, workflowInstance.getRunStatus());
        verify(workflowInstanceRepository).save(workflowInstance);
    }

    @Test
    void updateRunStatus_updatesWorkflowStepInstanceStatus() {
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        RunStatus runStatus = RunStatus.COMPLETED;

        instanceController.updateRunStatus(stepInstance, runStatus);

        assertEquals(RunStatus.COMPLETED, stepInstance.getRunStatus());
        verify(workflowStepInstanceRepository).save(stepInstance);
    }

    @Test
    void setEndTime_setsEndTimeForWorkflowInstance() {
        WorkflowInstance workflowInstance = new WorkflowInstance();

        instanceController.setEndTime(workflowInstance);

        assertNotNull(workflowInstance.getEndTime());
        verify(workflowInstanceRepository).save(workflowInstance);
    }

    @Test
    void setStartTime_setsStartTimeForWorkflowInstance() {
        WorkflowInstance workflowInstance = new WorkflowInstance();

        instanceController.setStartTime(workflowInstance);

        assertNotNull(workflowInstance.getStartTime());
        verify(workflowInstanceRepository).save(workflowInstance);
    }

    @Test
    void setEndTime_setsEndTimeForWorkflowStepInstance() {
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();

        instanceController.setEndTime(stepInstance);

        assertNotNull(stepInstance.getEndTime());
        verify(workflowStepInstanceRepository).save(stepInstance);
    }

    @Test
    void setStartTime_setsStartTimeForWorkflowStepInstance() {
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();

        instanceController.setStartTime(stepInstance);

        assertNotNull(stepInstance.getStartTime());
        verify(workflowStepInstanceRepository).save(stepInstance);
    }

    @Test
    void updateOutputData_updatesWorkflowInstanceOutputData() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        Map<String, Object> outputData = new HashMap<>();

        instanceController.updateOutputData(workflowInstance, outputData);

        assertEquals(outputData, workflowInstance.getOutputData());
        verify(workflowInstanceRepository).save(workflowInstance);
    }

    @Test
    void setFailureReason_setsFailureReasonForWorkflowInstance() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        String failureReason = "failureReason";

        instanceController.setFailureReason(workflowInstance, failureReason);

        assertEquals(failureReason, workflowInstance.getFailureReason());
        verify(workflowInstanceRepository).save(workflowInstance);
    }

    @Test
    void deleteLastWorkflowInstances_deletesOldWorkflowInstances() {
        List<WorkflowInstance> instances = new ArrayList<>();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setStartTime(LocalDateTime.now().minusDays(10));
        instances.add(instance);

        when(workflowInstanceRepository.findAll()).thenReturn(instances);

        instanceController.deleteLastWorkflowInstances(5, 1);

        verify(workflowInstanceRepository).deleteAll(anyList());
    }

    @Test
    void getNextWorkflowStepInstance_returnsNextStepInstance() {
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setControlStructure(ControlStructure.SIMPLE);
        stepInstance.setNextStepId("nextStepId");

        when(workflowStepInstanceRepository.findByUid("nextStepId")).thenReturn(new WorkflowStepInstance());

        WorkflowStepInstance result = instanceController.getNextWorkflowStepInstance(stepInstance);

        assertNotNull(result);
    }

    @Test
    void getStartStep_returnsFirstStepInstance() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setUid("workflowInstanceUid");
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setRunOrderLabel(RunOrderLabel.FIRST);

        when(workflowStepInstanceRepository.findByWorkflowInstanceUid("workflowInstanceUid")).thenReturn(List.of(stepInstance));

        WorkflowStepInstance result = instanceController.getStartStep(workflowInstance);

        assertEquals(stepInstance, result);
    }

    @Test
    void getStartStep_throwsException_whenFirstStepNotFound() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setUid("workflowInstanceUid");

        when(workflowStepInstanceRepository.findByWorkflowInstanceUid("workflowInstanceUid")).thenReturn(Collections.emptyList());

        assertThrows(InternalServiceException.class, () -> instanceController.getStartStep(workflowInstance));
    }
}