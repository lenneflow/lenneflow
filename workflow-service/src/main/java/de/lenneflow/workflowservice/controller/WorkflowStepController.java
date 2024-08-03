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

    @GetMapping("/get-step/{uuid}")
    public WorkflowStep getStep(@PathVariable String uuid) {
        return workflowStepRepository.findByUid(uuid);
    }

    @GetMapping("/get-workflow-steps/{workflowId}")
    public List<WorkflowStep> getAllWorkflowSteps(@PathVariable String workflowId) {
        return workflowStepRepository.findByWorkflowId(workflowId);
    }

    @GetMapping("/get-all-steps")
    public List<WorkflowStep> getAllWorkflows() {
        return workflowStepRepository.findAll();
    }

    @PostMapping("/add-step")
    public WorkflowStep addNewWorkflow(@RequestBody WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }

    @PatchMapping("/update-step")
    public WorkflowStep patchWorkflow(@RequestBody WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }
}
