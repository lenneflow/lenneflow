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

    public boolean evaluateBooleanExpression(String workflowInstanceUid, String expression) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression);
        return result.getBooleanValue();
    }

    public String evaluateStringExpression(String workflowInstanceUid, String expression) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression);
        return result.getStringValue();

    }

    public double evaluateDoubleExpression(String workflowInstanceUid, String expression) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression);
        return result.getNumberValue().doubleValue();

    }

    public void normalizeInputData(Map<String, Object> inputData, String workflowInstanceUid) {
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                normalizeInputData((Map<String, Object>) entry.getValue(), workflowInstanceUid);
            } else if (entry.getValue() instanceof String) {
                Object newValue = evaluateInputDataEntry(workflowInstanceUid, (String) entry.getValue());
                inputData.put(entry.getKey(), newValue);
            }
        }
    }

    private Object evaluateInputDataEntry(String workflowInstanceUid, String value) {
        if(value.toLowerCase().startsWith(FORMULA_PREFIX)){
            String expression = value.replace(FORMULA_PREFIX, "").replace("#","");
            EvaluationValue eval = evaluateExpression(workflowInstanceUid, expression);
            if(eval.isBooleanValue())
                return eval.getBooleanValue();
            if(eval.isNumberValue())
                return eval.getNumberValue().intValue();
            if(eval.isDurationValue())
                return eval.getDurationValue();
            return eval.getValue().toString();

        }
        return value;
    }

    private EvaluationValue evaluateExpression(String workflowInstanceUid, String expression) {
        String[] subStrings = StringUtils.substringsBetween(expression, "[", "]");
        for(String s : subStrings) {
            expression = expression.replace(s, getDataFromSubstring(workflowInstanceUid, s));
        }
        expression = expression.replace("[", "").replace("]", "");
        Expression exp = new Expression(expression);
        try {
            return exp.evaluate();
        } catch (Exception e) {
            throw new InternalServiceException("Invalid expression in Payload: " + expression);
        }
    }

    private String getDataFromSubstring(String workflowInstanceUid, String dataPath) {
        String[] stringParts = dataPath.split("\\.");
        WorkflowStepInstance step = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid(stringParts[0].trim(), workflowInstanceUid);
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
