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

    public final static String FORMULA_PREFIX = "@formula#";

    public ExpressionEvaluator(WorkflowStepInstanceRepository workflowStepInstanceRepository) {
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
    }

    public boolean evaluateBooleanExpression(String workflowUid, String expression) {
        Object result = evaluateExpression(workflowUid, expression);
        return Boolean.parseBoolean(result.toString());
    }

    public String evaluateStringExpression(String workflowUid, String expression) {
        Object result = evaluateExpression(workflowUid, expression);
        return result.toString();

    }

    public double evaluateDoubleExpression(String workflowUid, String expression) {
        Object result = evaluateExpression(workflowUid, expression);
        return Double.parseDouble(result.toString());

    }

    public void normalizeInputData(Map<String, Object> inputData, String workflowUid) {
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                normalizeInputData((Map<String, Object>) entry.getValue(), workflowUid);
            } else if (entry.getValue() instanceof String) {
                Object newValue = evaluateInputDataEntry(workflowUid, (String) entry.getValue());
                inputData.put(entry.getKey(), newValue);
            }
        }
    }

    private Object evaluateInputDataEntry(String workflowUid, String value) {
        if(value.toLowerCase().startsWith(FORMULA_PREFIX)){
            String expression = value.replace(FORMULA_PREFIX, "").replace("#","");
            return evaluateExpression(workflowUid, expression);
        }
        return value;
    }

    private Object evaluateExpression(String workflowUid, String expression) {
        String[] subStrings = StringUtils.substringsBetween(expression, "[", "]");
        for(String s : subStrings) {
            expression = expression.replace(s, getDataFromSubstring(workflowUid, s));
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

    private String getDataFromSubstring(String workflowUid, String dataPath) {
        String[] stringParts = dataPath.split("\\.");
        WorkflowStepInstance step = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid(stringParts[0].trim(), workflowUid);
        return switch (stringParts[1].toLowerCase().trim()) {
            case "output", "outputdata" -> {
                Map<String, Object> outputData = step.getOutputData();
                yield outputData.get(stringParts[2].trim()).toString();
            }
            case "input", "inputdata" -> {
                Map<String, Object> inputData = step.getInputData();
                yield inputData.get(stringParts[2].trim()).toString();
                //TODO get deeper located data
            }
            default -> throw new InternalServiceException("Invalid data path: " + dataPath);
        };


    }



}
