package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow/task")
public class WorkflowTaskController {

    final
    WorkflowStepRepository workflowStepRepository;

    public WorkflowTaskController(WorkflowStepRepository workflowStepRepository) {
        this.workflowStepRepository = workflowStepRepository;
    }

    @GetMapping("/get/{uuid}")
    public WorkflowStep getWorkflow(@PathVariable String uuid) {
        return workflowStepRepository.findByUuid(uuid);
    }

    @GetMapping("/get/all")
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
