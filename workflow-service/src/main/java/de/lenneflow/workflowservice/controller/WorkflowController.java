package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    final
    WorkflowRepository workflowRepository;

    public WorkflowController(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @GetMapping("/")
    public String home(@PathVariable String uuid) {
        return "Workflow service is working!";
    }

    @GetMapping("/get/{uuid}")
    public Workflow getWorkflow(@PathVariable String uuid) {
        return workflowRepository.findByWorkflowId(uuid);
    }

    @GetMapping("/get-all")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping("/add")
    public Workflow addNewWorkflow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @PatchMapping("/update")
    public Workflow patchWorkflow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }
}
