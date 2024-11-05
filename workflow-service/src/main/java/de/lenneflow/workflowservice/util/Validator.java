package de.lenneflow.workflowservice.util;

import com.ezylang.evalex.Expression;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.format.IriFormat;
import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import de.lenneflow.workflowservice.exception.InternalServiceException;
import de.lenneflow.workflowservice.exception.PayloadNotValidException;
import de.lenneflow.workflowservice.exception.ResourceNotFoundException;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.JsonSchemaRepository;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class Validator {

    Logger logger = LoggerFactory.getLogger(Validator.class);

    final WorkflowRepository workflowRepository;
    final WorkflowStepRepository workflowStepRepository;
    final JsonSchemaRepository jsonSchemaRepository;

    public Validator(WorkflowRepository workflowRepository, WorkflowStepRepository workflowStepRepository, JsonSchemaRepository jsonSchemaRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.jsonSchemaRepository = jsonSchemaRepository;
    }

    public void validate(WorkflowStep workflowStep) {
        checkMandatoryFields(workflowStep);
        checkUniqueValues(workflowStep);
        checkWorkflowExists(workflowStep);
        checkExpressions(workflowStep);
    }


    public void validate(Workflow workflow) {
        String workflowName = workflow.getName();
        if (workflowName == null || workflowName.isEmpty()) {
            throw new PayloadNotValidException("Workflow name is empty");
        }
        if (workflowRepository.findByName(workflowName) != null) {
            throw new PayloadNotValidException("Workflow with name " + workflowName + " already exists");
        }
    }

    private void checkUniqueValues(WorkflowStep workflowStep) {
        if (workflowStepRepository.findByNameAndWorkflowUid(workflowStep.getName(), workflowStep.getWorkflowUid()) != null) {
            throw new PayloadNotValidException("The workflow step name already exists for the associated workflow uid: " + workflowStep.getWorkflowUid());
        }
    }

    private void checkExpressions(WorkflowStep workflowStep) {
        switch (workflowStep.getControlStructure()) {
            case SWITCH:
                String switchCondition = workflowStep.getSwitchCase();
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
        for (String s : subStrings) {
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
        if (workflowRepository.findByUid(workflowStep.getWorkflowUid()) == null) {
            throw new ResourceNotFoundException("The workflow associated with this workflow step does not exist!");
        }
    }

    private void checkMandatoryFields(WorkflowStep workflowStep) {

        checkGeneralMandatoryFields(workflowStep);

        switch (workflowStep.getControlStructure()) {
            case SIMPLE:
                if (workflowStep.getFunctionId() == null || workflowStep.getFunctionId().isEmpty()) {
                    logger.info("Simple Workflow step {} has no function ID", workflowStep.getName());
                    throw new PayloadNotValidException("The field functionId is mandatory for this payload!");
                }
                break;
            case SWITCH:
                if (workflowStep.getSwitchCase() == null || workflowStep.getSwitchCase().isEmpty()) {
                    logger.info("Workflow step {} has no switch condition", workflowStep.getName());
                    throw new PayloadNotValidException("The field switchCondition is mandatory for this payload!");
                }
                if (workflowStep.getDecisionCases() == null || workflowStep.getDecisionCases().isEmpty()) {
                    logger.info("Workflow step {} has no decision cases", workflowStep.getName());
                    throw new PayloadNotValidException("The object decisionCases is mandatory for this payload!");
                }
                break;
            case DO_WHILE:
                if (workflowStep.getFunctionId() == null || workflowStep.getFunctionId().isEmpty()) {
                    logger.info("Workflow step {} has no function ID", workflowStep.getName());
                    throw new PayloadNotValidException("The field functionId is mandatory for this payload!");
                }
                if (workflowStep.getStopCondition() == null || workflowStep.getStopCondition().isEmpty()) {
                    logger.info("Workflow step {} has no stop condition", workflowStep.getName());
                    throw new PayloadNotValidException("The field stopCondition is mandatory for this payload!");
                }
                break;
            case SUB_WORKFLOW:
                if (workflowStep.getSubWorkflowId() == null || workflowStep.getSubWorkflowId().isEmpty()) {
                    logger.info("Workflow step {} has no sub-workflow ID", workflowStep.getName());
                    throw new PayloadNotValidException("The field subWorkflowId is mandatory for this payload!");
                }
                break;
            default:
                break;
        }
    }

    private void checkGeneralMandatoryFields(WorkflowStep workflowStep) {
        if (workflowStep.getUid() == null || workflowStep.getUid().isEmpty()) {
            logger.info("Workflow step {} has no UID", workflowStep.getName());
            throw new InternalServiceException("UID for this payload was not generated by the system!");
        }
        if (workflowStep.getWorkflowUid() == null || workflowStep.getWorkflowUid().isEmpty()) {
            logger.info("Workflow step {} has no UID", workflowStep.getName());
            throw new PayloadNotValidException("The field workflowUid is mandatory for this payload!");
        }
        if (workflowStep.getName() == null || workflowStep.getName().isEmpty()) {
            logger.info("Workflow step {} has no step name", workflowStep.getName());
            throw new PayloadNotValidException("The field name is mandatory for this payload!");
        }
        if (workflowStep.getExecutionOrder() <= 0) {
            logger.info("Workflow step {} has no positive execution order", workflowStep.getName());
            throw new PayloadNotValidException("The field executionOrder must have a value greater than 0!");
        }
    }

    public void validateJsonSchema(JsonSchema jsonSchema){
        try {
            JsonSchemaVersion version = jsonSchema.getSchemaVersion();
            SpecVersion.VersionFlag versionFlag = SpecVersion.VersionFlag.valueOf(version.toString());
            JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(versionFlag);
            SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
            SchemaValidatorsConfig config = builder.build();
            com.networknt.schema.JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(getSchemaId(versionFlag)), config);
            Set<ValidationMessage> assertions = schema.validate(jsonSchema.getSchema(), InputFormat.JSON, executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
            if (!assertions.isEmpty()) {
                StringBuilder message = new StringBuilder();
                assertions.forEach(x -> message.append(x.getMessage()).append("\n"));
                throw new PayloadNotValidException("The json schema " + jsonSchema.getName() + " is not valid!\n" + message);
            }
        } catch (Exception e) {
            throw new PayloadNotValidException("Schema parse error " + e.getMessage());
        }

    }


    public void validate(WorkflowDTO workflowDTO) {
        if (workflowDTO.getInputDataSchemaUid() == null || workflowDTO.getInputDataSchemaUid().isEmpty()) {
            throw new PayloadNotValidException("Please provide the input data schema uid!");
        }
        JsonSchema inputSchema = jsonSchemaRepository.findByUid(workflowDTO.getInputDataSchemaUid());
        if (inputSchema == null) {
            throw new PayloadNotValidException("input data schema does not exist!");
        }
        if (workflowDTO.getOutputDataSchemaUid() == null || workflowDTO.getOutputDataSchemaUid().isEmpty()) {
            throw new PayloadNotValidException("Please provide the output data schema uid!");
        }
        JsonSchema outputSchema = jsonSchemaRepository.findByUid(workflowDTO.getOutputDataSchemaUid());
        if (outputSchema == null) {
            throw new PayloadNotValidException("output data schema does not exist!");
        }
    }

    private String getSchemaId(SpecVersion.VersionFlag versionFlag) {
        switch (versionFlag) {
            case V4 -> {
                return SchemaId.V4;
            }
            case V6 -> {
                return SchemaId.V6;
            }
            case V7 -> {
                return SchemaId.V7;
            }
            case V201909 -> {
                return SchemaId.V201909;
            }
            case V202012 -> {
                return SchemaId.V202012;
            }
        }
        throw new InternalServiceException("Schema version " + versionFlag + " is not known!");
    }
}


