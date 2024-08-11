package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    final
    WorkflowRepository workflowRepository;

    public WorkflowController(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @GetMapping(value={"", "/"})
    public String checkService() {
        return "Welcome to the Workflow Service! Everything is working fine!";
    }

    @GetMapping("/get/{uuid}")
    public Workflow getWorkflow(@PathVariable String uuid) {
        return workflowRepository.findByWorkflowId(uuid);
    }

    @GetMapping("/get/name/{workflowName}")
    public Workflow getWorkflowByName(@PathVariable String workflowName) {
        return workflowRepository.findByName(workflowName);
    }

    @GetMapping("/get/all")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping("/add")
    public Workflow addNewWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @PatchMapping("/update")
    public Workflow patchWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }
}
