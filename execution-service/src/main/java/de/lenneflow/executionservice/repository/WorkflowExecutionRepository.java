package de.lenneflow.executionservice.repository;


import de.lenneflow.executionservice.model.WorkflowExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowExecutionRepository extends MongoRepository<WorkflowExecution, String> {

    WorkflowExecution findByExecutionID(String id);

    List<WorkflowExecution> findByWorkflowID(String workflowId);

    List<WorkflowExecution> findByWorkflowStatus(String status);

}
