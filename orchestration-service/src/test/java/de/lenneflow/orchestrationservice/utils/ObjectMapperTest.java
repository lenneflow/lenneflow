package de.lenneflow.orchestrationservice.utils;

import de.lenneflow.orchestrationservice.dto.GlobalInputDataDto;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    @Test
    void mapToWorkflowInstance_returnsCorrectWorkflowInstance() {
        Workflow workflow = new Workflow();
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");
        workflow.setUid("12345");
        workflow.setRestartable(true);
        workflow.setTimeOutInSeconds(3600);

        WorkflowInstance result = ObjectMapper.mapToWorkflowInstance(workflow);

        assertEquals("Test Workflow", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("12345", result.getWorkflowUid());
        assertTrue(result.isRestartable());
        assertEquals(3600, result.getTimeOutInSeconds());
    }

    @Test
    void mapToGlobalInputData_returnsCorrectGlobalInputData() {
        GlobalInputDataDto globalInputDataDto = new GlobalInputDataDto();
        globalInputDataDto.setName("Test Name");
        globalInputDataDto.setDescription("Test Description");
        globalInputDataDto.setInputData(Map.of("key", "value"));

        GlobalInputData result = ObjectMapper.mapToGlobalInputData(globalInputDataDto);

        assertEquals("Test Name", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Map.of("key", "value"), result.getInputData());
    }

    @Test
    void mapFunctionQueueElement_returnsCorrectQueueElement() {
        Function function = new Function();
        function.setName("Test Function");
        function.setCpuRequest("500m");
        function.setMemoryRequest("256Mi");
        function.setServiceUrl("http://test-service");

        QueueElement result = ObjectMapper.mapFunctionQueueElement(function);

        assertEquals("Test Function", result.getFunctionName());
        assertEquals("500m", result.getCpuRequest());
        assertEquals("256Mi", result.getMemoryRequest());
        assertEquals("http://test-service", result.getServiceUrl());
    }

    @Test
    void mapFunctionQueueElement_handlesNullFunctionType() {
        Function function = new Function();
        function.setName("Test Function");
        function.setCpuRequest("500m");
        function.setMemoryRequest("256Mi");
        function.setServiceUrl("http://test-service");

        QueueElement result = ObjectMapper.mapFunctionQueueElement(function);

        assertNull(result.getFunctionType());
    }
}