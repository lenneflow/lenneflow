package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.feignmodels.WorkflowStep;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import de.lenneflow.executionservice.repository.WorkflowExecutionRepository;
import de.lenneflow.executionservice.repository.WorkflowInstanceRepository;
import de.lenneflow.executionservice.repository.WorkflowStepInstanceRepository;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InstanceController {

    final TaskServiceClient taskServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;

    public InstanceController(TaskServiceClient taskServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController) {
        this.taskServiceClient = taskServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
    }

    public WorkflowInstance createWorkflowInstance(String workflowId, Map<String, Object> inputParameters) {
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowInstance workflowInstance = new WorkflowInstance(workflow, inputParameters);
        workflowInstanceRepository.save(workflowInstance);
        workflowInstance.setStatus(WorkflowStatus.NOT_RUN);
        List<WorkflowStep> steps = workflowServiceClient.getWorkflowSteps(workflowId);
        Map<String, String> stepStepInstanceMapping = new HashMap<>();

        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = new WorkflowStepInstance(step, workflowInstance.getUid());
            stepStepInstanceMapping.put(step.getUid(), stepInstance.getUid());
            workflowStepInstanceRepository.save(stepInstance);
        }
        List<String> stepInstanceIds = updateWorkflowStepInstances(workflowId, steps, stepStepInstanceMapping);
        workflowInstance.setStepInstanceIds(stepInstanceIds);
        return workflowInstanceRepository.save(workflowInstance);
    }

    private List<String> updateWorkflowStepInstances(String workflowId, List<WorkflowStep> steps, Map<String, String> stepStepInstanceMapping) {
        List<String> stepInstanceIds = new ArrayList<>();
        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getUid()));
            WorkflowStep nextStep = workflowServiceClient.getWorkflowStep(workflowId, step.getNextStepId());
            if (nextStep != null) {
                WorkflowStepInstance nextStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getNextStepId()));
                stepInstance.setNextStepId(nextStepInstance.getUid());
            }
            WorkflowStep previousStep = workflowServiceClient.getWorkflowStep(workflowId, step.getPreviousStepId());
            if (previousStep != null) {
                WorkflowStepInstance previousStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getPreviousStepId()));
                stepInstance.setPreviousStepId(previousStepInstance.getUid());
            }
            Map<String, String> decisionCases = step.getDecisionCases();
            if (decisionCases != null && !decisionCases.isEmpty()) {
                Map<String, String> decisionCaseInstances = new HashMap<>();
                for (Map.Entry<String, String> entry : decisionCases.entrySet()) {
                    String decisionCaseId = entry.getValue();
                    WorkflowStepInstance decisionCaseInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(decisionCaseId));
                    decisionCaseInstances.put(entry.getKey(), decisionCaseInstance.getUid());
                }
                stepInstance.setDecisionCases(decisionCaseInstances);
            }
            workflowStepInstanceRepository.save(stepInstance);
            stepInstanceIds.add(stepInstance.getUid());
        }
        return stepInstanceIds;
    }
}
