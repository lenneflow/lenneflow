package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstance, String> {

    WorkflowInstance findByUid(String id);

    WorkflowInstance findByWorkflowName(String name);

    List<WorkflowInstance> findByOwnerEmail(String email);

    List<WorkflowInstance> findByStatusListenerEnabled(boolean enabled);

}
