package de.lenneflow.orchestrationservice.utils;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExpressionEvaluator {

    final
    WorkflowStepInstanceRepository workflowStepInstanceRepository;

    public ExpressionEvaluator(WorkflowStepInstanceRepository workflowStepInstanceRepository) {
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
    }

    public boolean evaluateBooleanExpression(WorkflowStepInstance stepInstance, String expression) {
        Object result = evaluateExpression(stepInstance, expression);
        return Boolean.parseBoolean(result.toString());
    }

    public String evaluateStringExpression(WorkflowStepInstance stepInstance, String expression) {
        Object result = evaluateExpression(stepInstance, expression);
        return result.toString();

    }

    public double evaluateDoubleExpression(WorkflowStepInstance stepInstance, String expression) {
        Object result = evaluateExpression(stepInstance, expression);
        return Double.parseDouble(result.toString());

    }

    private Object evaluateExpression(WorkflowStepInstance stepInstance, String expression) {
        String[] subStrings = StringUtils.substringsBetween(expression, "[", "]");
        for(String s : subStrings) {
            expression = expression.replace(s, getDataFromSubstring(stepInstance, s));
        }
        expression = expression.replace("[", "").replace("]", "");
        Expression exp = new Expression(expression);
        try {
            EvaluationValue value = exp.evaluate();
            return value.getValue();
        } catch (Exception e) {
            throw new InternalServiceException("Invalid expression in Payload: " + expression);
        }
    }

    private String getDataFromSubstring(WorkflowStepInstance stepInstance, String dataPath) {
        String[] stringParts = dataPath.split("\\.");
        WorkflowStepInstance step = workflowStepInstanceRepository.findByStepNameAndWorkflowInstanceUid(stringParts[0].trim(), stepInstance.getWorkflowUid());
        return switch (stringParts[1].toLowerCase().trim()) {
            case "output" -> {
                Map<String, Object> outputData = step.getOutputData();
                yield outputData.get(stringParts[2].trim()).toString();
            }
            case "input" -> {
                Map<String, Object> inputData = step.getInputData();
                yield inputData.get(stringParts[2].trim()).toString();
            }
            default -> throw new InternalServiceException("Invalid data path: " + dataPath);
        };


    }



}
