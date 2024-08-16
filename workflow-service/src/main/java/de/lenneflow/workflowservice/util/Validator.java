package de.lenneflow.workflowservice.util;

import com.ezylang.evalex.Expression;
import de.lenneflow.workflowservice.exception.InternalServiceException;
import de.lenneflow.workflowservice.exception.PayloadNotValidException;
import de.lenneflow.workflowservice.exception.ResourceNotFoundException;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    Logger logger = LoggerFactory.getLogger(Validator.class);

    final WorkflowRepository workflowRepository;
    final WorkflowStepRepository workflowStepRepository;

    public Validator(WorkflowRepository workflowRepository, WorkflowStepRepository workflowStepRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
    }

    public void validateWorkflowStep(WorkflowStep workflowStep){
        checkMandatoryFields(workflowStep);
        checkUniqueValues(workflowStep);
        checkWorkflowExists(workflowStep);
        checkExpressions(workflowStep);
    }


    public void validateWorkflow(Workflow workflow){
        String workflowName = workflow.getName();
        if(workflowName == null || workflowName.isEmpty()){
            throw new PayloadNotValidException("Workflow name is empty");
        }
        if(workflowRepository.findByName(workflowName) != null){
            throw new PayloadNotValidException("Workflow with name " + workflowName + " already exists");
        }
    }

    private void checkUniqueValues(WorkflowStep workflowStep) {
        if(workflowStepRepository.findByStepNameAndWorkflowUid(workflowStep.getStepName(), workflowStep.getWorkflowUid()) != null){
            throw new PayloadNotValidException("The workflow step name already exists for the associated workflow uid: " + workflowStep.getWorkflowUid());
        }
    }

    private void checkExpressions(WorkflowStep workflowStep) {
        switch (workflowStep.getWorkFlowStepType()){
            case SWITCH:
                String switchCondition = workflowStep.getSwitchCondition();
                validateExpression(switchCondition);
                break;
            case DO_WHILE:
                String stopCondition = workflowStep.getStopCondition();
                validateExpression(stopCondition);
                break;
            default:
                break;
        }
    }

    private void validateExpression(String expression) {
        String[] subStrings = StringUtils.substringsBetween(expression, "[", "]");
        for(String s : subStrings) {
            expression = expression.replace(s, "0");
        }
        expression = expression.replace("[", "").replace("]", "");
        Expression exp = new Expression(expression);
        try {
            exp.evaluate();
        } catch (Exception e) {
            throw new PayloadNotValidException("Invalid expression in Payload: " + expression);
        }
    }



    private void checkWorkflowExists(WorkflowStep workflowStep) {
        if(workflowRepository.findByUid(workflowStep.getUid()) == null){
            throw new ResourceNotFoundException("The workflow associated with this workflow step does not exist!");
        }
    }

    private void checkMandatoryFields(WorkflowStep workflowStep) {

        checkGeneralMandatoryFields(workflowStep);

        switch (workflowStep.getWorkFlowStepType()){
            case SIMPLE:
                if(workflowStep.getFunctionId() == null || workflowStep.getFunctionId().isEmpty()){
                    logger.info("Simple Workflow step {} has no function ID", workflowStep.getStepName());
                    throw new PayloadNotValidException("The field functionId is mandatory for this payload!");
                }
                break;
            case SWITCH:
                if(workflowStep.getSwitchCondition() == null || workflowStep.getSwitchCondition().isEmpty()){
                    logger.info("Workflow step {} has no switch condition", workflowStep.getStepName());
                    throw new PayloadNotValidException("The field switchCondition is mandatory for this payload!");
                }
                if(workflowStep.getDecisionCases() == null || workflowStep.getDecisionCases().isEmpty()){
                    logger.info("Workflow step {} has no decision cases", workflowStep.getStepName());
                    throw new PayloadNotValidException("The object decisionCases is mandatory for this payload!");
                }
                break;
            case DO_WHILE:
                if(workflowStep.getFunctionId() == null || workflowStep.getFunctionId().isEmpty()){
                    logger.info("Workflow step {} has no function ID", workflowStep.getStepName());
                    throw new PayloadNotValidException("The field functionId is mandatory for this payload!");
                }
                if(workflowStep.getStopCondition() == null || workflowStep.getStopCondition().isEmpty()){
                    logger.info("Workflow step {} has no stop condition", workflowStep.getStepName());
                    throw new PayloadNotValidException("The field stopCondition is mandatory for this payload!");
                }
                break;
            case SUB_WORKFLOW:
                if(workflowStep.getSubWorkflowId() == null || workflowStep.getSubWorkflowId().isEmpty()){
                    logger.info("Workflow step {} has no sub-workflow ID", workflowStep.getStepName());
                    throw new PayloadNotValidException("The field subWorkflowId is mandatory for this payload!");
                }
                break;
            default:
                break;
        }
    }

    private void checkGeneralMandatoryFields(WorkflowStep workflowStep) {
        if(workflowStep.getUid() == null || workflowStep.getUid().isEmpty()){
            logger.info("Workflow step {} has no UID", workflowStep.getStepName());
            throw new InternalServiceException("UID for this payload was not generated by the system!");
        }
        if(workflowStep.getWorkflowUid() == null || workflowStep.getWorkflowUid().isEmpty()){
            logger.info("Workflow step {} has no UID", workflowStep.getStepName());
            throw new PayloadNotValidException("The field workflowUid is mandatory for this payload!");
        }
        if(workflowStep.getStepName() == null || workflowStep.getStepName().isEmpty()){
            logger.info("Workflow step {} has no step name", workflowStep.getStepName());
            throw new PayloadNotValidException("The field stepName is mandatory for this payload!");
        }
        if(workflowStep.getExecutionOrder() <= 0){
            logger.info("Workflow step {} has no positive execution order", workflowStep.getStepName());
            throw new PayloadNotValidException("The field executionOrder must have a value greater than 0!");
        }
    }
}


