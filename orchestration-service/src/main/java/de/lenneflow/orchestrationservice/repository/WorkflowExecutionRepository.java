package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for the workflow execution entity
 *
 * @author Idrissa Ganemtore
 */
public interface WorkflowExecutionRepository extends MongoRepository<WorkflowExecution, String> {

    WorkflowExecution findByRunId(String id);
}
