package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface WorkflowStepRepository extends MongoRepository<WorkflowStep, String> {

    WorkflowStep findByUuid(String uuid);

    WorkflowStep findByName(String name);

    List<WorkflowStep> findByStepType(WorkFlowStepType type);

}
