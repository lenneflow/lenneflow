package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
public class WorkflowStepController {

    final
    WorkflowStepRepository workflowStepRepository;

    public WorkflowStepController(WorkflowStepRepository workflowStepRepository) {
        this.workflowStepRepository = workflowStepRepository;
    }

    @GetMapping("/get_step/{uuid}")
    public WorkflowStep getStep(@PathVariable String uuid) {
        return workflowStepRepository.findByStepId(uuid);
    }

    @GetMapping("/get_workflow_steps/{workflowId}")
    public List<WorkflowStep> getAllWorkflowSteps(@PathVariable String workflowId) {
        return workflowStepRepository.findByWorkflowId(workflowId);
    }

    @GetMapping("/get_all_steps")
    public List<WorkflowStep> getAllWorkflows() {
        return workflowStepRepository.findAll();
    }

    @PostMapping("/add")
    public WorkflowStep addNewWorkflow(WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }

    @PatchMapping("/update")
    public WorkflowStep patchWorkflow(WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }
}
