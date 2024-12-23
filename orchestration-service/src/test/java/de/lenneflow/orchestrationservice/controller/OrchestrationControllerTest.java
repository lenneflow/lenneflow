package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.dto.GlobalInputDataDto;
import de.lenneflow.orchestrationservice.enums.JsonSchemaVersion;
import de.lenneflow.orchestrationservice.exception.PayloadNotValidException;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.JsonSchema;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.helpercomponents.InstanceController;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
import de.lenneflow.orchestrationservice.dto.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.repository.GlobalInputDataRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.helpercomponents.WorkflowRunner;
import de.lenneflow.orchestrationservice.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrchestrationControllerTest {

    @Mock
    private WorkflowServiceClient workflowServiceClient;
    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Mock
    private WorkflowRunner workflowRunner;
    @Mock
    private InstanceController instanceController;
    @Mock
    private GlobalInputDataRepository globalInputDataRepository;

    private OrchestrationController orchestrationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orchestrationController = new OrchestrationController(workflowServiceClient, workflowInstanceRepository, workflowRunner, instanceController, globalInputDataRepository);
    }

    @Test
    void startWorkflowGet_shouldStartWorkflow() {
        String workflowId = "workflowUid";
        String inputdataId = "inputDataUid";
        GlobalInputData globalInputData = new GlobalInputData();
        Workflow workflow = new Workflow();
        JsonSchema jsonSchema = new JsonSchema();
        workflow.setInputDataSchema(jsonSchema);
        jsonSchema.setSchemaVersion(JsonSchemaVersion.V4);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowExecution workflowExecution = new WorkflowExecution(workflowInstance);

        when(globalInputDataRepository.findByUid(inputdataId)).thenReturn(globalInputData);
        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(workflow);
        when(instanceController.generateWorkflowInstance(workflow, globalInputData.getInputData(), null, null)).thenReturn(workflowInstance);
        when(workflowRunner.startWorkflow(workflowInstance)).thenReturn(workflowExecution);

        try (MockedStatic<Validator> mockStatic = Mockito.mockStatic(Validator.class)) {

            WorkflowExecution result = orchestrationController.startWorkflowGet(workflowId, inputdataId);

            assertNotNull(result);
            assertEquals(workflowExecution, result);
        }

    }

    @Test
    void startWorkflowGet_shouldThrowExceptionWhenGlobalInputDataNotFound() {
        String workflowId = "workflowUid";
        String inputdataId = "inputDataUid";

        when(globalInputDataRepository.findByUid(inputdataId)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.startWorkflowGet(workflowId, inputdataId));
    }

    @Test
    void startWorkflowGet_shouldThrowExceptionWhenWorkflowNotFound() {
        String workflowId = "workflowUid";
        String inputdataId = "inputDataUid";
        GlobalInputData globalInputData = new GlobalInputData();

        when(globalInputDataRepository.findByUid(inputdataId)).thenReturn(globalInputData);
        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.startWorkflowGet(workflowId, inputdataId));
    }

    @Test
    void startWorkflowGet2_shouldStartWorkflow() {
        String workflowId = "workflowUid";
        Workflow workflow = new Workflow();
        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowExecution workflowExecution = new WorkflowExecution(workflowInstance);

        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(workflow);
        when(instanceController.generateWorkflowInstance(workflow, null, null, null)).thenReturn(workflowInstance);
        when(workflowRunner.startWorkflow(workflowInstance)).thenReturn(workflowExecution);

        WorkflowExecution result = orchestrationController.startWorkflowGet2(workflowId);

        assertNotNull(result);
        assertEquals(workflowExecution, result);
    }

    @Test
    void startWorkflowGet2_shouldThrowExceptionWhenWorkflowNotFound() {
        String workflowId = "workflowUid";

        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.startWorkflowGet2(workflowId));
    }

    @Test
    void startWorkflowPost_shouldStartWorkflow() {
        String workflowId = "workflowUid";
        Map<String, Object> inputData = new HashMap<>();
        Workflow workflow = new Workflow();
        JsonSchema jsonSchema = new JsonSchema();
        workflow.setInputDataSchema(jsonSchema);
        jsonSchema.setSchemaVersion(JsonSchemaVersion.V4);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        WorkflowExecution workflowExecution = new WorkflowExecution(workflowInstance);

        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(workflow);
        when(instanceController.generateWorkflowInstance(workflow, inputData, null, null)).thenReturn(workflowInstance);
        when(workflowRunner.startWorkflow(workflowInstance)).thenReturn(workflowExecution);

        try (MockedStatic<Validator> mockStatic = Mockito.mockStatic(Validator.class)) {
            WorkflowExecution result = orchestrationController.startWorkflowPost(workflowId, inputData);

            assertNotNull(result);
            assertEquals(workflowExecution, result);
        }

    }

    @Test
    void startWorkflowPost_shouldThrowExceptionWhenWorkflowNotFound() {
        String workflowId = "workflowUid";
        Map<String, Object> inputData = new HashMap<>();

        when(workflowServiceClient.getWorkflowById(workflowId)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.startWorkflowPost(workflowId, inputData));
    }

    @Test
    void stopWorkflow_shouldStopWorkflow() {
        String executionId = "executionUid";
        WorkflowExecution workflowExecution = new WorkflowExecution();

        when(workflowRunner.stopWorkflow(executionId)).thenReturn(workflowExecution);

        WorkflowExecution result = orchestrationController.stopWorkflow(executionId);

        assertNotNull(result);
        assertEquals(workflowExecution, result);
    }

    @Test
    void pauseWorkflow_shouldPauseWorkflow() {
        String executionId = "executionUid";
        WorkflowExecution workflowExecution = new WorkflowExecution();

        when(workflowRunner.pauseWorkflow(executionId)).thenReturn(workflowExecution);

        WorkflowExecution result = orchestrationController.pauseWorkflow(executionId);

        assertNotNull(result);
        assertEquals(workflowExecution, result);
    }

    @Test
    void resumeWorkflow_shouldResumeWorkflow() {
        String executionId = "executionUid";
        WorkflowExecution workflowExecution = new WorkflowExecution();

        when(workflowRunner.resumeWorkflow(executionId)).thenReturn(workflowExecution);

        WorkflowExecution result = orchestrationController.resumeWorkflow(executionId);

        assertNotNull(result);
        assertEquals(workflowExecution, result);
    }

    @Test
    void workflowRunState_shouldReturnCurrentExecutionState() {
        String executionId = "executionUid";
        WorkflowExecution workflowExecution = new WorkflowExecution();

        when(workflowRunner.getCurrentExecutionState(executionId)).thenReturn(workflowExecution);

        WorkflowExecution result = orchestrationController.workflowRunState(executionId);

        assertNotNull(result);
        assertEquals(workflowExecution, result);
    }

    @Test
    void executionList_shouldReturnExecutionList() {
        List<WorkflowInstance> instances = List.of(new WorkflowInstance(), new WorkflowInstance());
        List<WorkflowExecution> executions = List.of(new WorkflowExecution(instances.get(0)), new WorkflowExecution(instances.get(1)));

        when(workflowInstanceRepository.findAll()).thenReturn(instances);

        List<WorkflowExecution> result = orchestrationController.executionList();

        assertNotNull(result);
        assertEquals(executions.size(), result.size());
    }

    @Test
    void createGlobalInputData_shouldCreateGlobalInputData() {
        GlobalInputDataDto globalInputDataDto = new GlobalInputDataDto();
        GlobalInputData globalInputData = new GlobalInputData();

        when(globalInputDataRepository.save(any(GlobalInputData.class))).thenReturn(globalInputData);
        try (MockedStatic<Validator> mockStatic = Mockito.mockStatic(Validator.class)) {
            GlobalInputData result = orchestrationController.createGlobalInputData(globalInputDataDto);

            assertNotNull(result);
            assertEquals(globalInputData, result);
        }

    }

    @Test
    void updateGlobalInputData_shouldUpdateGlobalInputData() {
        String inputDataUid = "inputDataUid";
        GlobalInputDataDto globalInputDataDto = new GlobalInputDataDto();
        GlobalInputData globalInputData = new GlobalInputData();
        GlobalInputData found = new GlobalInputData();

        when(globalInputDataRepository.findByUid(inputDataUid)).thenReturn(found);
        when(globalInputDataRepository.save(any(GlobalInputData.class))).thenReturn(globalInputData);
        try (MockedStatic<Validator> mockStatic = Mockito.mockStatic(Validator.class)) {
            GlobalInputData result = orchestrationController.updateGlobalInputData(globalInputDataDto, inputDataUid);
            assertNotNull(result);
            assertEquals(globalInputData, result);
        }


    }

    @Test
    void updateGlobalInputData_shouldThrowExceptionWhenInputDataNotFound() {
        String inputDataUid = "inputDataUid";
        GlobalInputDataDto globalInputDataDto = new GlobalInputDataDto();

        when(globalInputDataRepository.findByUid(inputDataUid)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.updateGlobalInputData(globalInputDataDto, inputDataUid));
    }

    @Test
    void getGlobalInputData_shouldReturnGlobalInputData() {
        String inputDataUid = "inputDataUid";
        GlobalInputData globalInputData = new GlobalInputData();

        when(globalInputDataRepository.findByUid(inputDataUid)).thenReturn(globalInputData);

        GlobalInputData result = orchestrationController.getGlobalInputData(inputDataUid);

        assertNotNull(result);
        assertEquals(globalInputData, result);
    }

    @Test
    void getGlobalInputData_shouldThrowExceptionWhenInputDataNotFound() {
        String inputDataUid = "inputDataUid";

        when(globalInputDataRepository.findByUid(inputDataUid)).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> orchestrationController.getGlobalInputData(inputDataUid));
    }
}