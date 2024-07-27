package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import de.lenneflow.workflowservice.model.WorkflowStep;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowStepRepository extends MongoRepository<WorkflowStep, String> {

    WorkflowStep findByStepId(String id);

    List<WorkflowStep> findByStepType(WorkFlowStepType type);

    List<WorkflowStep> findByWorkflowId(String workflowId);

}
