package de.lenneflow.executionservice.repository;


import de.lenneflow.executionservice.enums.WorkFlowStepType;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowStepInstanceRepository extends MongoRepository<WorkflowStepInstance, String> {

    WorkflowStepInstance findById(String id);

    WorkflowStepInstance findByWorkflowStepId(String id);

    List<WorkflowStepInstance> findByStepType(WorkFlowStepType type);

    List<WorkflowStepInstance> findByWorkflowInstanceId(String workflowInstanceId);

}
