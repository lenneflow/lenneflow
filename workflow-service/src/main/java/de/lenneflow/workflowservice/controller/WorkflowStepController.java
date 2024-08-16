package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.SimpleWorkflowStep;
import de.lenneflow.workflowservice.dto.SubWorkflowStep;
import de.lenneflow.workflowservice.dto.SwitchWorkflowStep;
import de.lenneflow.workflowservice.dto.WhileWorkflowStep;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.Validator;
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
    private final Validator validator;
    private final ModelMapper modelMapper = new ModelMapper();

    public WorkflowStepController(WorkflowStepRepository workflowStepRepository, WorkflowRepository workflowRepository, Validator validator) {
        this.workflowStepRepository = workflowStepRepository;
        this.workflowRepository = workflowRepository;
        this.validator = validator;
    }

    @GetMapping("/{id}")
    public WorkflowStep getStep(@PathVariable String id) {
        return workflowStepRepository.findByUid(id);
    }

    @GetMapping
    public WorkflowStep getStepByName(@RequestParam String name, @RequestParam(name = "workflow-id") String workflowId) {
        return workflowStepRepository.findByStepNameAndWorkflowUid(name, workflowId);
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
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = saveWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SimpleWorkflowStep.class);
    }

    @PostMapping("/switch")
    public SwitchWorkflowStep addSwitchWorkflowStep(@RequestBody SwitchWorkflowStep switchWorkflowStep) {
        WorkflowStep workflowStep = modelMapper.map(switchWorkflowStep, WorkflowStep.class);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = saveWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SwitchWorkflowStep.class);
    }

    @PostMapping("/while")
    public WhileWorkflowStep addWhileWorkflowStep(@RequestBody WhileWorkflowStep whileWorkflowStep) {
        WorkflowStep workflowStep = modelMapper.map(whileWorkflowStep, WorkflowStep.class);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = saveWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, WhileWorkflowStep.class);
    }

    @PostMapping("/sub-workflow")
    public SubWorkflowStep addSubWorkflowStep(@RequestBody SubWorkflowStep subWorkflowStep) {
        WorkflowStep workflowStep = modelMapper.map(subWorkflowStep, WorkflowStep.class);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = saveWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SubWorkflowStep.class);
    }

    @PatchMapping("/simple/{id}")
    public SimpleWorkflowStep updateWorkflowStep(@PathVariable String id, @RequestBody SimpleWorkflowStep simpleWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(id);
        modelMapper.map(simpleWorkflowStep, workflowStep);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = patchWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SimpleWorkflowStep.class);
    }

    @PatchMapping("/switch/{id}")
    public SwitchWorkflowStep updateWorkflowStep(@PathVariable String id, @RequestBody SwitchWorkflowStep switchWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(id);
        modelMapper.map(switchWorkflowStep, workflowStep);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = patchWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SwitchWorkflowStep.class);
    }

    @PatchMapping("/while/{id}")
    public WhileWorkflowStep updateWorkflowStep(@PathVariable String id, @RequestBody WhileWorkflowStep whileWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(id);
        modelMapper.map(whileWorkflowStep, workflowStep);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = patchWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, WhileWorkflowStep.class);
    }

    @PatchMapping("/sub-workflow/{id}")
    public SubWorkflowStep updateWorkflowStep(@PathVariable String id, @RequestBody SubWorkflowStep subWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(id);
        modelMapper.map(subWorkflowStep, workflowStep);
        validator.validateWorkflowStep(workflowStep);
        WorkflowStep savedWorkflowStep = patchWorkflowStep(workflowStep);
        return modelMapper.map(savedWorkflowStep, SubWorkflowStep.class);
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
        return workflowStepRepository.save(workflowStep);
    }
}
