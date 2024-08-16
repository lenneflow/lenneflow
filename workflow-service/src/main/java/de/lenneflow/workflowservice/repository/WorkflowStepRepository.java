package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.WorkflowStep;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowStepRepository extends MongoRepository<WorkflowStep, String> {

    WorkflowStep findByUid(String id);

    WorkflowStep findByNameAndWorkflowUid(String stepName, String workflowUid);

    List<WorkflowStep> findByWorkflowUid(String workflowId);

    List<WorkflowStep> findByWorkflowName(String workflowName);

}
