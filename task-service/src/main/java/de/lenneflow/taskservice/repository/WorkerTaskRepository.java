package de.lenneflow.taskservice.repository;

import de.lenneflow.taskservice.model.WorkerTask;
import de.lenneflow.taskservice.util.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerTaskRepository extends MongoRepository<WorkerTask, String> {

    WorkerTask findByTaskID(String taskId);

    WorkerTask findByTaskName(String taskName);

    List<WorkerTask> findByTaskStatus(TaskStatus status);

    List<WorkerTask> findByTaskType(String taskType);

    List<WorkerTask> findByTaskPriority(int priority);

}
