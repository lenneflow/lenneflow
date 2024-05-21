package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.model.WorkflowTask;
import de.lenneflow.workflowservice.repository.WorkflowTaskRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow/task")
public class WorkflowTaskController {

    final
    WorkflowTaskRepository workflowTaskRepository;

    public WorkflowTaskController(WorkflowTaskRepository workflowTaskRepository) {
        this.workflowTaskRepository = workflowTaskRepository;
    }

    @GetMapping("/get/{uuid}")
    public WorkflowTask getWorkflow(@PathVariable String uuid) {
        return workflowTaskRepository.findByUuid(uuid);
    }

    @GetMapping("/get/all")
    public List<WorkflowTask> getAllWorkflows() {
        return workflowTaskRepository.findAll();
    }

    @PostMapping("/add")
    public WorkflowTask addNewWorkflow(WorkflowTask workflowTask) {
        return workflowTaskRepository.save(workflowTask);
    }

    @PatchMapping("/update")
    public WorkflowTask patchWorkflow(WorkflowTask workflowTask) {
        return workflowTaskRepository.save(workflowTask);
    }
}
