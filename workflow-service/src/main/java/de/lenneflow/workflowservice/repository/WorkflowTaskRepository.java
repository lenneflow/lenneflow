package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowTask;
import de.lenneflow.workflowservice.util.WorkFlowTaskType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowTaskRepository extends MongoRepository<WorkflowTask, String> {

    WorkflowTask findByUuid(String uuid);

    WorkflowTask findByName(String name);

    List<WorkflowTask> findByTaskType(WorkFlowTaskType type);

}
