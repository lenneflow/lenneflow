package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository for the workflow step instance entity
 *
 * @author Idrissa Ganemtore
 */
public interface WorkflowStepInstanceRepository extends MongoRepository<WorkflowStepInstance, String> {

    WorkflowStepInstance findByUid(String uid);

    WorkflowStepInstance findByNameAndWorkflowInstanceUid(String stepName, String workflowInstanceUid);

    List<WorkflowStepInstance> findByWorkflowInstanceUid(String workflowInstanceId);

}
