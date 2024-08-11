package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowStepController {

    final WorkflowStepRepository workflowStepRepository;
    final WorkflowRepository workflowRepository;

    public WorkflowStepController(WorkflowStepRepository workflowStepRepository, WorkflowRepository workflowRepository) {
        this.workflowStepRepository = workflowStepRepository;
        this.workflowRepository = workflowRepository;
    }

    @GetMapping("/step/get/id/{uuid}")
    public WorkflowStep getStep(@PathVariable String uuid) {
        return workflowStepRepository.findByUid(uuid);
    }

    @GetMapping("/step/get/name/{name}")
    public WorkflowStep getStepByName(@PathVariable String name) {
        return workflowStepRepository.findByStepName(name);
    }

    @GetMapping("/step/list/workflow/id/{workflowId}")
    public List<WorkflowStep> getAllWorkflowSteps(@PathVariable String workflowId) {
        return workflowStepRepository.findByWorkflowId(workflowId);
    }

    @GetMapping("/step/list/workflow/name/{workflowName}")
    public List<WorkflowStep> getAllWorkflowStepsByWorkflowName(@PathVariable String workflowName) {
        return workflowStepRepository.findByWorkflowName(workflowName);
    }

    @GetMapping("/step/get/all")
    public List<WorkflowStep> getAllWorkflows() {
        return workflowStepRepository.findAll();
    }

    @PostMapping("/step/add")
    public WorkflowStep addNewWorkflow(@RequestBody WorkflowStep workflowStep) {
        WorkflowStep saved = workflowStepRepository.save(workflowStep);
        Workflow workflow = workflowRepository.findByName(saved.getWorkflowName());
        List<WorkflowStep> stepList = workflow.getSteps();
        stepList.add(saved);
        workflow.setSteps(stepList);
        workflowRepository.save(workflow);
        return saved;
    }

    @PatchMapping("/step/update")
    public WorkflowStep patchWorkflow(@RequestBody WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }
}
