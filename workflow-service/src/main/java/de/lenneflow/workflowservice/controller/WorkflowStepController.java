package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.SimpleWorkflowStep;
import de.lenneflow.workflowservice.dto.SubWorkflowStep;
import de.lenneflow.workflowservice.dto.SwitchWorkflowStep;
import de.lenneflow.workflowservice.dto.WhileWorkflowStep;
import de.lenneflow.workflowservice.enums.ControlStructure;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.ObjectMapper;
import de.lenneflow.workflowservice.util.Validator;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/step")
public class WorkflowStepController {

    final WorkflowStepRepository workflowStepRepository;
    final WorkflowRepository workflowRepository;
    private final Validator validator;

    public WorkflowStepController(WorkflowStepRepository workflowStepRepository, WorkflowRepository workflowRepository, Validator validator) {
        this.workflowStepRepository = workflowStepRepository;
        this.workflowRepository = workflowRepository;
        this.validator = validator;
    }

    @GetMapping("/{uid}")
    public WorkflowStep getStep(@PathVariable("uid") String stepId) {
        return workflowStepRepository.findByUid(stepId);
    }

    @GetMapping("/name/{step-name}/workflow-uid/{workflow-uid}")
    public WorkflowStep getStepByNameAndWorkflowId(@PathVariable("step-name") String name, @PathVariable("workflow-uid") String workflowId) {
        return workflowStepRepository.findByNameAndWorkflowUid(name, workflowId);
    }

    @GetMapping("/workflow-uid/{workflow-uid}")
    public List<WorkflowStep> getWorkflowStepsByWorkflowID(@PathVariable("workflow-uid") String workflowId) {
        return workflowStepRepository.findByWorkflowUid(workflowId);
    }

    @GetMapping("/workflow-name/{workflow-name}")
    public List<WorkflowStep> getAllWorkflowStepsByWorkflowName(@PathVariable("workflow-name") String workflowName) {
        return workflowStepRepository.findByWorkflowName(workflowName);
    }

    @GetMapping("/list")
    public List<WorkflowStep> getAllWorkflowSteps() {
        return workflowStepRepository.findAll();
    }

    @PostMapping("/simple/create")
    public WorkflowStep addSimpleWorkflowStep(@RequestBody SimpleWorkflowStep simpleWorkflowStep) {
        WorkflowStep workflowStep = ObjectMapper.mapToWorkflowStep(simpleWorkflowStep);
        workflowStep.setUid(UUID.randomUUID().toString());
        workflowStep.setCreated(LocalDateTime.now());
        workflowStep.setUpdated(LocalDateTime.now());
        workflowStep.setControlStructure(ControlStructure.SIMPLE);
        validator.validate(workflowStep);
        return saveWorkflowStep(workflowStep);
    }

    @PostMapping("/switch/create")
    public WorkflowStep addSwitchWorkflowStep(@RequestBody SwitchWorkflowStep switchWorkflowStep) {
        WorkflowStep workflowStep = ObjectMapper.mapToWorkflowStep(switchWorkflowStep);
        workflowStep.setUid(UUID.randomUUID().toString());
        workflowStep.setCreated(LocalDateTime.now());
        workflowStep.setUpdated(LocalDateTime.now());
        workflowStep.setControlStructure(ControlStructure.SWITCH);
        validator.validate(workflowStep);
        return saveWorkflowStep(workflowStep);
    }

    @PostMapping("/while/create")
    public WorkflowStep addWhileWorkflowStep(@RequestBody WhileWorkflowStep whileWorkflowStep) {
        WorkflowStep workflowStep = ObjectMapper.mapToWorkflowStep(whileWorkflowStep);
        workflowStep.setUid(UUID.randomUUID().toString());
        workflowStep.setCreated(LocalDateTime.now());
        workflowStep.setUpdated(LocalDateTime.now());
        workflowStep.setControlStructure(ControlStructure.DO_WHILE);
        validator.validate(workflowStep);
        return saveWorkflowStep(workflowStep);
    }

    @PostMapping("/sub-workflow/create")
    public WorkflowStep addSubWorkflowStep(@RequestBody SubWorkflowStep subWorkflowStep) {
        WorkflowStep workflowStep = ObjectMapper.mapToWorkflowStep(subWorkflowStep);
        workflowStep.setUid(UUID.randomUUID().toString());
        workflowStep.setCreated(LocalDateTime.now());
        workflowStep.setUpdated(LocalDateTime.now());
        workflowStep.setControlStructure(ControlStructure.SUB_WORKFLOW);
        validator.validate(workflowStep);
        return saveWorkflowStep(workflowStep);
    }

    @PostMapping("/simple/{uid}/update")
    public WorkflowStep updateWorkflowStep(@PathVariable String uid, @RequestBody SimpleWorkflowStep simpleWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(uid);
        ObjectMapper.mapToWorkflowStep(workflowStep, simpleWorkflowStep);
        validator.validate(workflowStep);
        return patchWorkflowStep(workflowStep);
    }

    @PostMapping("/switch/{uid}/update")
    public WorkflowStep updateWorkflowStep(@PathVariable String uid, @RequestBody SwitchWorkflowStep switchWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(uid);
        ObjectMapper.mapToWorkflowStep(workflowStep, switchWorkflowStep);
        validator.validate(workflowStep);
        return patchWorkflowStep(workflowStep);
    }

    @PostMapping("/while/{uid}/update")
    public WorkflowStep updateWorkflowStep(@PathVariable String uid, @RequestBody WhileWorkflowStep whileWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(uid);
        ObjectMapper.mapToWorkflowStep(workflowStep, whileWorkflowStep);
        validator.validate(workflowStep);
        return patchWorkflowStep(workflowStep);
    }

    @PostMapping("/sub-workflow/{uid}/update")
    public WorkflowStep updateWorkflowStep(@PathVariable String uid, @RequestBody SubWorkflowStep subWorkflowStep) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(uid);
        ObjectMapper.mapToWorkflowStep(workflowStep, subWorkflowStep);
        validator.validate(workflowStep);
        return patchWorkflowStep(workflowStep);
    }

    @DeleteMapping("/{uid}")
    public void deleteWorkflowStep(@PathVariable String uid) {
        WorkflowStep workflowStep = workflowStepRepository.findByUid(uid);
        workflowStepRepository.delete(workflowStep);
    }


    private WorkflowStep saveWorkflowStep(WorkflowStep workflowStep) {
        workflowStep.setCreated(LocalDateTime.now());
        workflowStep.setUpdated(LocalDateTime.now());
        WorkflowStep saved = workflowStepRepository.save(workflowStep);
        Workflow workflow = workflowRepository.findByUid(saved.getWorkflowUid());
        List<WorkflowStep> stepList = workflow.getSteps();
        stepList.add(saved);
        workflow.setSteps(stepList);
        workflowRepository.save(workflow);
        return saved;
    }

    private WorkflowStep patchWorkflowStep(WorkflowStep workflowStep) {
        workflowStep.setUpdated(LocalDateTime.now());
        return workflowStepRepository.save(workflowStep);
    }
}
