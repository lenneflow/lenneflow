package de.lenneflow.orchestrationservice.utils;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Expression evaluator class. In a workflow many input data can be expressions. for example [step1.output.waitTime].
 * The expression evaluator will get the wait time value from the output of the run step "step1".
 *
 * @author Idrissa Ganemtore
 */
@Component
public class ExpressionEvaluator {

    final
    WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final ObjectMapper objectMapper = new ObjectMapper();


    public ExpressionEvaluator(WorkflowStepInstanceRepository workflowStepInstanceRepository, WorkflowInstanceRepository workflowInstanceRepository) {
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    /**
     * This method will go throw an input data and replace all the expression by their real values.
     *
     * @param inputData           the input data to normalize
     * @param workflowInstanceUid the ID of the workflow instance.
     */
    public void normalizeInputData(Map<String, Object> inputData, String workflowInstanceUid) {
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                normalizeInputData((Map<String, Object>) entry.getValue(), workflowInstanceUid);
            } else if (entry.getValue() instanceof String string) {
                Object newValue = evaluateInputDataEntry(workflowInstanceUid, string);
                inputData.put(entry.getKey(), newValue);
            }
        }
    }

    /**
     * Evaluates an input data entry. Checks if the data is an expression. In this case,
     * it will read the value of the expression. Otherwise, it will simply return the value.
     *
     * @param workflowInstanceUid the ID of the instance
     * @param value               the data to evaluate
     * @return the evaluation result.
     */
    private Object evaluateInputDataEntry(String workflowInstanceUid, String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            String expression = value.replace("[", "").replace("]", "");
            return readDataFromPath(workflowInstanceUid, expression);
        }
        return value;
    }

    /**
     * Reads data from a given expression path like [step2.input.key.value]
     *
     * @param workflowInstanceUid the ID of the instance
     * @param dataPath            the data path
     * @return the result of the evaluation
     */
    public Object readDataFromPath(String workflowInstanceUid, String dataPath) {
        dataPath = dataPath.replace("[", "").replace("]", "");
        String[] stringParts = dataPath.split("\\.");

        if(stringParts[0].trim().equalsIgnoreCase("workflow") && (stringParts[1].trim().equalsIgnoreCase("input") || stringParts[1].trim().equalsIgnoreCase("inputData"))){
            WorkflowInstance instance = workflowInstanceRepository.findByUid(workflowInstanceUid);
            if(instance == null){
                throw new InternalServiceException("Workflow instance not found for uid: " + workflowInstanceUid);
            }
            Map<String, Object> inputData = instance.getInputData();
            return getMapValueByPath(inputData, getJsonPath(stringParts));

        }
        WorkflowStepInstance step = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid(stringParts[0].trim(), workflowInstanceUid);
        if(step == null){
            throw new InternalServiceException("Workflow step was not found for workflow instance uid: " + workflowInstanceUid);
        }
        return switch (stringParts[1].toLowerCase().trim()) {
            case "output", "outputdata" -> {
                Map<String, Object> outputData = step.getOutputData();
                yield getMapValueByPath(outputData, getJsonPath(stringParts));
            }
            case "input", "inputdata" -> {
                Map<String, Object> inputData = step.getInputData();
                yield getMapValueByPath(inputData, getJsonPath(stringParts));
            }
            default -> throw new InternalServiceException("Invalid data path: " + dataPath);
        };
    }

    /**
     * Creates a json path from a list of string.
     *
     * @param data the string list
     * @return the json path
     */
    private String getJsonPath(String[] data) {
        StringBuilder result = new StringBuilder("$");
        for (int i = 2; i < data.length; i++) {
            result.append(".").append(data[i]);
        }
        return result.toString();
    }

    /**
     * Gets a value from a json object by using a json path.
     *
     * @param inputData the json object
     * @param path      the path to search
     * @return the value
     */
    private Object getMapValueByPath(Map<String, Object> inputData, String path) {
        try {
            String json = objectMapper.writeValueAsString(inputData);
            return JsonPath.read(json, path);
        } catch (JsonProcessingException e) {
            throw new InternalServiceException("Could not get data from Map " + inputData.toString());
        }
    }

    /**
     * The same line the method evaluateExpression but return a boolean value.
     *
     * @param workflowInstanceUid The UID of the workflow instance
     * @param expression          the expression to evaluate
     * @return the boolean value
     */
    public boolean evaluateDoWhileCondition(String workflowInstanceUid, String expression, int currentRunCount) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression, currentRunCount);
        return result.getBooleanValue();
    }

    /**
     * The same line the method evaluateExpression but return a string value.
     *
     * @param workflowInstanceUid The UID of the workflow instance
     * @param expression          the expression to evaluate
     * @return the string value
     */
    public String evaluateStringExpression(String workflowInstanceUid, String expression) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression, 0);
        return result.getStringValue();

    }

    /**
     * The same line the method evaluateExpression but return a double value.
     *
     * @param workflowInstanceUid The UID of the workflow instance
     * @param expression          the expression to evaluate
     * @return the double value
     */
    public double evaluateDoubleExpression(String workflowInstanceUid, String expression) {
        EvaluationValue result = evaluateExpression(workflowInstanceUid, expression, 0);
        return result.getNumberValue().doubleValue();

    }

    /**
     * Evaluates an expression et returns the value.
     *
     * @param workflowInstanceUid The UID of the workflow instance
     * @param expression          the expression to evaluate
     * @return the {@link EvaluationValue}
     */
    private EvaluationValue evaluateExpression(String workflowInstanceUid, String expression, int currentRunCount) {
        String[] subStrings = StringUtils.substringsBetween(expression, "[", "]");
        for (String s : subStrings) {
            if(s.equalsIgnoreCase("runCount")){
                expression = expression.replace("["+s+"]", currentRunCount+"");
            }else{
                expression = expression.replace(s, readDataFromPath(workflowInstanceUid, s).toString());
            }
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
