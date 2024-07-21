package de.lenneflow.taskservice.repository;

import de.lenneflow.taskservice.enums.TaskStatus;
import de.lenneflow.taskservice.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    Task findByTaskID(String taskId);

    Task findByTaskName(String taskName);

    List<Task> findByTaskStatus(TaskStatus status);

    List<Task> findByTaskType(String taskType);

    List<Task> findByTaskPriority(int priority);

}
