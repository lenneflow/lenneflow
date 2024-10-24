package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.ObjectMapper;
import de.lenneflow.workflowservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    final
    WorkflowRepository workflowRepository;
    final WorkflowStepRepository workflowStepRepository;
    private final Validator validator;

    public WorkflowController(WorkflowRepository workflowRepository, WorkflowStepRepository workflowStepRepository, Validator validator) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.validator = validator;
    }

    @GetMapping("/{id}")
    public Workflow getWorkflow(@PathVariable String id) {
        return workflowRepository.findByUid(id);
    }

    @GetMapping("/workflow-name/{workflow-name}")
    public Workflow getWorkflowByName(@PathVariable("workflow-name") String name) {
        return workflowRepository.findByName(name);
    }

    @GetMapping("/list")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping
    public Workflow addNewWorkflow(@RequestBody WorkflowDTO workflowDTO) {
        Workflow workflow = ObjectMapper.mapToWorkflow(workflowDTO);
        workflow.setUid(UUID.randomUUID().toString());
        workflow.setCreated(LocalDateTime.now());
        workflow.setUpdated(LocalDateTime.now());
        validator.validateWorkflow(workflow);
        return workflowRepository.save(workflow);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkflow(@PathVariable String id) {
        Workflow workflow = workflowRepository.findByUid(id);
        workflowStepRepository.deleteAll(workflow.getSteps());
        workflowRepository.delete(workflow);
    }

}
