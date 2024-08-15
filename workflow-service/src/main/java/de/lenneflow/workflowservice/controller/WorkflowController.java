package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    final
    WorkflowRepository workflowRepository;

    public WorkflowController(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Workflow Service!";
    }

    @GetMapping("/{id}")
    public Workflow getWorkflow(@PathVariable String id) {
        return workflowRepository.findByUid(id);
    }

    @GetMapping
    public Workflow getWorkflowByName(@RequestParam String name) {
        return workflowRepository.findByName(name);
    }

    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping
    public Workflow addNewWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @PatchMapping("/{id}")
    public Workflow patchWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    @DeleteMapping
    public void deleteWorkflow(@PathVariable String id) {
        Workflow workflow = workflowRepository.findByUid(id);
        workflowRepository.delete(workflow);
    }
}
