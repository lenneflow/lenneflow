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
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setOutputData(Map.of("value", "replacedValue"));
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(stepInstance);

        evaluator.normalizeInputData(inputData, "workflow1");

        assertEquals("replacedValue", inputData.get("key"));
    }

    @Test
    void normalizeInputData_withNestedExpressions_replacesWithValues() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("key", Map.of("nestedKey", "[step1.output.value]"));
        WorkflowStepInstance stepInstance = new WorkflowStepInstance();
        stepInstance.setOutputData(Map.of("value", "replacedValue"));
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(stepInstance);

        evaluator.normalizeInputData(inputData, "workflow1");

        assertEquals("replacedValue", ((Map) inputData.get("key")).get("nestedKey"));
    }

    @Test
    void readDataFromPath_withInvalidStep_throwsInternalServiceException() {
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(null);

        assertThrows(InternalServiceException.class, () -> evaluator.readDataFromPath("workflow1", "step1.output.value"));
    }

    @Test
    void evaluateDoWhileCondition_withValidExpression_returnsBoolean() throws EvaluationException, ParseException {
        when(instanceRepo.findByUid("workflow1")).thenReturn(new WorkflowInstance());
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(new WorkflowStepInstance());

        boolean result = evaluator.evaluateDoWhileCondition("workflow1", "[step1.output.value] == true", 1);

        assertTrue(result);
    }

    @Test
    void evaluateStringExpression_withValidExpression_returnsString() throws EvaluationException, ParseException {
        when(instanceRepo.findByUid("workflow1")).thenReturn(new WorkflowInstance());
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(new WorkflowStepInstance());

        String result = evaluator.evaluateStringExpression("workflow1", "[step1.output.value]");

        assertEquals("expectedString", result);
    }

    @Test
    void evaluateDoubleExpression_withValidExpression_returnsDouble() throws EvaluationException, ParseException {
        when(instanceRepo.findByUid("workflow1")).thenReturn(new WorkflowInstance());
        when(stepRepo.findByNameAndWorkflowInstanceUid("step1", "workflow1")).thenReturn(new WorkflowStepInstance());

        double result = evaluator.evaluateDoubleExpression("workflow1", "[step1.output.value]");

        assertEquals(1.0, result);
    }
}