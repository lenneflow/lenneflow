package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper = new ModelMapper();

    public WorkflowController(WorkflowRepository workflowRepository, WorkflowStepRepository workflowStepRepository, Validator validator) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.validator = validator;
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

    @GetMapping("/all")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping
    public WorkflowDTO addNewWorkflow(@RequestBody WorkflowDTO workflowDTO) {
        Workflow workflow = modelMapper.map(workflowDTO, Workflow.class);
        workflow.setUid(UUID.randomUUID().toString());
        validator.validateWorkflow(workflow);
        workflow.setCreated(LocalDateTime.now());
        workflow.setUpdated(LocalDateTime.now());
        return modelMapper.map(workflowRepository.save(workflow), WorkflowDTO.class);
    }

    @PatchMapping("/{id}")
    public WorkflowDTO patchWorkflow(@RequestBody WorkflowDTO workflowDTO, @PathVariable String id) {
        Workflow workflow = workflowRepository.findByUid(id);
        modelMapper.map(workflowDTO, workflow);
        validator.validateWorkflow(workflow);
        workflow.setUpdated(LocalDateTime.now());
        return modelMapper.map(workflowRepository.save(workflow), WorkflowDTO.class);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkflow(@PathVariable String id) {
        Workflow workflow = workflowRepository.findByUid(id);
        workflowStepRepository.deleteAll(workflow.getSteps());
        workflowRepository.delete(workflow);
    }
}
