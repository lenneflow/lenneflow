package de.lenneflow.taskservice.controller;

import de.lenneflow.taskservice.model.WorkerTask;
import de.lenneflow.taskservice.repository.WorkerTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class WorkerTaskController {

    final
    WorkerTaskRepository workerTaskRepository;

    public WorkerTaskController(WorkerTaskRepository workerTaskRepository) {
        this.workerTaskRepository = workerTaskRepository;
    }

    @GetMapping("/get/{id}")
    public WorkerTask getWorkerTaskById(@PathVariable String id) {
        return workerTaskRepository.findById(id).orElse(null);
    }

    @GetMapping("/get/all")
    public List<WorkerTask> getAllWorkerTasks() {
        return workerTaskRepository.findAll();
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public void addWorkerTask(@RequestBody WorkerTask task) {
        task.setTaskID(UUID.randomUUID().toString());
        workerTaskRepository.save(task);
    }

    @PatchMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateWorkerTask(@RequestBody WorkerTask task) {
        workerTaskRepository.save(task);
    }
}
