package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.SimpleWorkflowStep;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.DTOMapper;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows/steps")
public class WorkflowStepController {

    final WorkflowStepRepository workflowStepRepository;
    final WorkflowRepository workflowRepository;

    private ModelMapper modelMapper = new ModelMapper();

    public WorkflowStepController(WorkflowStepRepository workflowStepRepository, WorkflowRepository workflowRepository) {
        this.workflowStepRepository = workflowStepRepository;
        this.workflowRepository = workflowRepository;
    }

    @GetMapping("/{id}")
    public WorkflowStep getStep(@PathVariable String id) {
        return workflowStepRepository.findByUid(id);
    }

    @GetMapping
    public WorkflowStep getStepByName(@RequestParam String name) {
        return workflowStepRepository.findByStepName(name);
    }

    @GetMapping
    public List<WorkflowStep> getWorkflowStepsByWorkflowID(@RequestParam(name = "workflow-id") String workflowId) {
        return workflowStepRepository.findByWorkflowId(workflowId);
    }

    @GetMapping
    public List<WorkflowStep> getAllWorkflowStepsByWorkflowName(@RequestParam(name = "workflow-name") String workflowName) {
        return workflowStepRepository.findByWorkflowName(workflowName);
    }

    @GetMapping
    public List<WorkflowStep> getAllWorkflowSteps() {
        return workflowStepRepository.findAll();
    }

    @PostMapping("/simple")
    public SimpleWorkflowStep addSimpleWorkflowStep(@RequestBody SimpleWorkflowStep simpleWorkflowStep) {
        WorkflowStep workflowStep = modelMapper.map(simpleWorkflowStep, WorkflowStep.class);
        WorkflowStep savedWorkflowStep = saveWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SimpleWorkflowStep.class);
    }

    @PatchMapping("/{id}")
    public WorkflowStep patchWorkflowStep(@PathVariable String id, @RequestBody WorkflowStep workflowStep) {
        return workflowStepRepository.save(workflowStep);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkflowStep(@PathVariable String id) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(id);
        workflowStepRepository.delete(workflowStep);
    }

    private WorkflowStep saveWorkflowStep(WorkflowStep workflowStep) {
        workflowStep.setUid(UUID.randomUUID().toString());
        workflowStep.setCreationTime(LocalDateTime.now());
        workflowStep.setUpdateTime(LocalDateTime.now());
        WorkflowStep saved = workflowStepRepository.save(workflowStep);
        Workflow workflow = workflowRepository.findByUid(saved.getWorkflowUid());
        List<WorkflowStep> stepList = workflow.getSteps();
        stepList.add(saved);
        workflow.setSteps(stepList);
        workflowRepository.save(workflow);
        return saved;
    }

    private WorkflowStep patchWorkflowStep(WorkflowStep workflowStep) {
        workflowStep.setUpdateTime(LocalDateTime.now());
        WorkflowStep saved = workflowStepRepository.save(workflowStep);
        return saved;
    }
}
