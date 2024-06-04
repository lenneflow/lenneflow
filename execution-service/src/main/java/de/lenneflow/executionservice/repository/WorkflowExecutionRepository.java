package de.lenneflow.executionservice.repository;


import de.lenneflow.executionservice.model.WorkflowExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkflowExecutionRepository extends MongoRepository<WorkflowExecution, String> {

    WorkflowExecution findByUid(String id);
}
