package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WorkflowServiceClient workflowServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workflowServiceClient = new WorkflowServiceClientImpl(restTemplate);
    }

    @Test
    void getWorkflowById_shouldReturnWorkflow() {
        String uid = "workflowUid";
        Workflow workflow = new Workflow();
        when(restTemplate.getForObject("/api/workflows/" + uid, Workflow.class)).thenReturn(workflow);

        Workflow result = workflowServiceClient.getWorkflowById(uid);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void getWorkflowByName_shouldReturnWorkflow() {
        String name = "workflowName";
        Workflow workflow = new Workflow();
        when(restTemplate.getForObject("/api/workflows/name/" + name, Workflow.class)).thenReturn(workflow);

        Workflow result = workflowServiceClient.getWorkflowByName(name);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void getWorkflowStepById_shouldReturnWorkflowStep() {
        String uid = "stepUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(restTemplate.getForObject("/api/workflows/steps/" + uid, WorkflowStep.class)).thenReturn(workflowStep);

        WorkflowStep result = workflowServiceClient.getWorkflowStepById(uid);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void getWorkflowStepByNameAndWorkflowId_shouldReturnWorkflowStep() {
        String stepName = "stepName";
        String workflowUid = "workflowUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(restTemplate.getForObject("/api/workflows/steps/name/" + stepName + "/workflow-uid/" + workflowUid, WorkflowStep.class)).thenReturn(workflowStep);

        WorkflowStep result = workflowServiceClient.getWorkflowStepByNameAndWorkflowId(stepName, workflowUid);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void getStepListByWorkflowId_shouldReturnStepList() {
        String workflowUid = "workflowUid";
        List<WorkflowStep> steps = List.of(new WorkflowStep(), new WorkflowStep());
        when(restTemplate.getForObject("/api/workflows/steps/workflow-uid/" + workflowUid, List.class)).thenReturn(steps);

        List<WorkflowStep> result = workflowServiceClient.getStepListByWorkflowId(workflowUid);

        assertNotNull(result);
        assertEquals(steps.size(), result.size());
    }

    @Test
    void getStepListByWorkflowName_shouldReturnStepList() {
        String workflowName = "workflowName";
        List<WorkflowStep> steps = List.of(new WorkflowStep(), new WorkflowStep());
        when(restTemplate.getForObject("/api/workflows/steps/workflow-name/" + workflowName, List.class)).thenReturn(steps);

        List<WorkflowStep> result = workflowServiceClient.getStepListByWorkflowName(workflowName);

        assertNotNull(result);
        assertEquals(steps.size(), result.size());
    }

    // Inner class to implement the WorkflowServiceClient interface
    private static class WorkflowServiceClientImpl implements WorkflowServiceClient {
        private final RestTemplate restTemplate;

        public WorkflowServiceClientImpl(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @Override
        public Workflow getWorkflowById(String uid) {
            return restTemplate.getForObject("/api/workflows/" + uid, Workflow.class);
        }

        @Override
        public Workflow getWorkflowByName(String name) {
            return restTemplate.getForObject("/api/workflows/name/" + name, Workflow.class);
        }

        @Override
        public WorkflowStep getWorkflowStepById(String uid) {
            return restTemplate.getForObject("/api/workflows/steps/" + uid, WorkflowStep.class);
        }

        @Override
        public WorkflowStep getWorkflowStepByNameAndWorkflowId(String stepName, String workflowUid) {
            return restTemplate.getForObject("/api/workflows/steps/name/" + stepName + "/workflow-uid/" + workflowUid, WorkflowStep.class);
        }

        @Override
        public List<WorkflowStep> getStepListByWorkflowId(String workflowUid) {
            return restTemplate.getForObject("/api/workflows/steps/workflow-uid/" + workflowUid, List.class);
        }

        @Override
        public List<WorkflowStep> getStepListByWorkflowName(String workflowName) {
            return restTemplate.getForObject("/api/workflows/steps/workflow-name/" + workflowName, List.class);
        }
    }
}