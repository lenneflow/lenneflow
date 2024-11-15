package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for the workflow instance entity
 *
 * @author Idrissa Ganemtore
 */
public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstance, String> {

    WorkflowInstance findByUid(String id);

}
