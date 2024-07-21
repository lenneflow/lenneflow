package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.Workflow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowRepository extends MongoRepository<Workflow, String> {

    Workflow findByWorkflowId(String id);

    Workflow findByName(String name);

    List<Workflow> findByOwnerEmail(String email);

    List<Workflow> findByStatusListenerEnabled(boolean enabled);

}
