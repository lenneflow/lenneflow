package de.lenneflow.orchestrationservice.utils;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
    final ObjectMapper objectMapper = new ObjectMapper();


    public ExpressionEvaluator(WorkflowStepInstanceRepository workflowStepInstanceRepository) {
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
    }

    public void normalizeInputData(Map<String, Object> inputData, String workflowInstanceUid) {
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                normalizeInputData((Map<String, Object>)entry.getValue(), workflowInstanceUid);
            } else if (entry.getValue() instanceof String string) {
                Object newValue = evaluateInputDataEntry(workflowInstanceUid, string);
                inputData.put(entry.getKey(), newValue);
            }
        }
    }

    private Object evaluateInputDataEntry(String workflowInstanceUid, String value) {
        if(value.startsWith("[") && value.endsWith("]")){
            String expression = value.replace("[", "").replace("]","");
            return getDataFromSubstring(workflowInstanceUid, expression);
        }
        return value;
    }

    private String getDataFromSubstring(String workflowInstanceUid, String dataPath) {
        String[] stringParts = dataPath.split("\\.");
        WorkflowStepInstance step = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid(stringParts[0].trim(), workflowInstanceUid);
        return switch (stringParts[1].toLowerCase().trim()) {
            case "output", "outputdata" -> {
                Map<String, Object> outputData = step.getOutputData();
                yield getMapValueByPath(outputData, getJsonPath(stringParts)).toString();
            }
            case "input", "inputdata" -> {
                Map<String, Object> inputData = step.getInputData();
                yield getMapValueByPath(inputData, getJsonPath(stringParts)).toString();
            }
            default -> throw new InternalServiceException("Invalid data path: " + dataPath);
        };
    }

    private String getJsonPath(String[] data){
        StringBuilder result = new StringBuilder("$");
        for(int i = 2; i < data.length; i++){
            result.append(".").append(data[i]);
        }
        return result.toString();
    }

    private Object getMapValueByPath(Map<String, Object> inputData, String path) {
        try {
            String json = objectMapper.writeValueAsString(inputData);
            return JsonPath.read(json, path);
        } catch (JsonProcessingException e) {
            throw new InternalServiceException("Could not get data from Map " + inputData.toString());
        }
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

}
