package de.lenneflow.orchestrationservice.utils;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpressionEvaluatorTests {

    WorkflowStepInstanceRepository stepRepo = mock(WorkflowStepInstanceRepository.class);
    WorkflowInstanceRepository instanceRepo = mock(WorkflowInstanceRepository.class);
    ExpressionEvaluator evaluator = new ExpressionEvaluator(stepRepo, instanceRepo);

    @Test
    void normalizeInputData_withValidExpressions_replacesWithValues() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("key", "[step1.output.value]");

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("value", "4");
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setOutputData(outputData);

        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(stepInstance);

        evaluator.normalizeInputData(inputData, "workflow1");

        assertEquals("4", inputData.get("key").toString());
    }

    @Test
    void normalizeInputData_withNestedExpressions_replacesWithValues() {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "[step1.output.value]*4");
        inputData.put("key", nested);

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("value", "4");

        WorkflowInstance instance = new WorkflowInstance();
        instance.setUid("workflow1");
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setName("step1");
        stepInstance.setOutputData(outputData);

        when(instanceRepo.findByUid("workflow1")).thenReturn(instance);
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(stepInstance);

        evaluator.normalizeInputData(inputData, "workflow1");

        assertEquals("16", ((Map) inputData.get("key")).get("nestedKey").toString());
    }

    @Test
    void readDataFromPath_withInvalidStep_throwsInternalServiceException() {
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(null);

        assertThrows(InternalServiceException.class, () -> evaluator.readDataFromPath("workflow1", "step1.output.value"));
    }

    @Test
    void  evaluateDoWhileCondition_withValidExpression_returnsBoolean() throws EvaluationException, ParseException {

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("value", "true");

        WorkflowInstance instance = new WorkflowInstance();
        instance.setUid("workflow1");

        WorkflowStepInstance step = new WorkflowStepInstance();
        step.setName("step1");
        step.setOutputData(outputData);

        when(instanceRepo.findByUid("workflow1")).thenReturn(instance);
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(step);

        boolean result = evaluator.evaluateDoWhileCondition("workflow1", "[step1.output.value] == true", 1);

        assertTrue(result);
    }

    @Test
    void evaluateStringExpression_withValidExpression_returnsString() throws EvaluationException, ParseException {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("value", "2=3");
        WorkflowInstance instance = new WorkflowInstance();
        instance.setUid("workflow1");
        WorkflowStepInstance step = new WorkflowStepInstance();
        step.setName("step1");
        step.setOutputData(outputData);
        when(instanceRepo.findByUid("workflow1")).thenReturn(instance);
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(step);

        String result = evaluator.evaluateStringExpression("workflow1", "[step1.output.value]");

        assertEquals("false", result);
    }

    @Test
    void evaluateDoubleExpression_withValidExpression_returnsDouble() throws EvaluationException, ParseException {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("value", "1.0");

        WorkflowInstance instance = new WorkflowInstance();
        instance.setUid("workflow1");

        WorkflowStepInstance step = new WorkflowStepInstance();
        step.setName("step1");
        step.setOutputData(outputData);

        when(instanceRepo.findByUid("workflow1")).thenReturn(instance);
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(step);

        double result = evaluator.evaluateDoubleExpression("workflow1", "[step1.output.value]");

        assertEquals(1.0, result);
    }
}