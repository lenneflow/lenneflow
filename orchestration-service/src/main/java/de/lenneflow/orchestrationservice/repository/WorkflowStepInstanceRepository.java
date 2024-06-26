package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowStepInstanceRepository extends MongoRepository<WorkflowStepInstance, String> {

    WorkflowStepInstance findByUid(String uid);

    WorkflowStepInstance findByWorkflowStepId(String id);

    List<WorkflowStepInstance> findByWorkflowInstanceId(String workflowInstanceId);

}
