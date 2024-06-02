package de.lenneflow.executionservice.repository;


import de.lenneflow.executionservice.model.WorkflowInstance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstance, String> {

    WorkflowInstance findByUid(String id);

    WorkflowInstance findByName(String name);

    List<WorkflowInstance> findByOwnerEmail(String email);

    List<WorkflowInstance> findByStatusListenerEnabled(boolean enabled);

}
