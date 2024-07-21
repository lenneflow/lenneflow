package de.lenneflow.taskservice.controller;

import de.lenneflow.taskservice.model.Task;
import de.lenneflow.taskservice.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class WorkerTaskController {

    final
    TaskRepository taskRepository;

    public WorkerTaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/get/{id}")
    public Task getWorkerTaskById(@PathVariable String id) {
        return taskRepository.findById(id).orElse(null);
    }

    @GetMapping("/get/all")
    public List<Task> getAllWorkerTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Task addWorkerTask(@RequestBody Task task) {
        task.setTaskID(UUID.randomUUID().toString());
        return taskRepository.save(task);
    }

    @PatchMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateWorkerTask(@RequestBody Task task) {
        taskRepository.save(task);
    }
}
