package de.lenneflow.taskservice.controller;

import de.lenneflow.taskservice.model.Task;
import de.lenneflow.taskservice.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
//@RequestMapping("/task")
public class TaskController {

    final
    TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/")
    public String home() {
        return "Task service is working!";
    }

    @GetMapping("task/get/{id}")
    public Task getWorkerTaskById(@PathVariable String id) {
        return taskRepository.findById(id).orElse(null);
    }

    @GetMapping("task/get/all")
    public List<Task> getAllWorkerTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("task/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Task addWorkerTask(@RequestBody Task task) {
        task.setTaskID(UUID.randomUUID().toString());
        return taskRepository.save(task);
    }

    @PatchMapping("task/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateWorkerTask(@RequestBody Task task) {
        taskRepository.save(task);
    }
}
