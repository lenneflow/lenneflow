package de.lenneflow.functionservice.controller;

import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.dto.JsonSchemaDTO;
import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.helpercomponents.DeploymentController;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.repository.JsonSchemaRepository;
import de.lenneflow.functionservice.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FunctionControllerTest {

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private Validator validator;

    @Mock
    private DeploymentController deploymentController;

    @Mock
    private WorkerServiceClient workerServiceClient;

    @Mock
    private JsonSchemaRepository jsonSchemaRepository;

    private FunctionController functionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        functionController = new FunctionController(functionRepository, validator, deploymentController, workerServiceClient, jsonSchemaRepository);
    }

    @Test
    void getFunctionById_shouldReturnFunctionWhenIdExists() {
        Function function = new Function();
        function.setUid("existingUid");
        when(functionRepository.findByUid("existingUid")).thenReturn(function);

        Function result = functionController.getFunctionById("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void getFunctionByName_shouldReturnFunctionWhenNameExists() {
        Function function = new Function();
        function.setName("existingName");
        when(functionRepository.findByName("existingName")).thenReturn(function);

        Function result = functionController.getFunctionByName("existingName");

        assertNotNull(result);
        assertEquals("existingName", result.getName());
    }

    @Test
    void getAllFunctions_shouldReturnListOfFunctions() {
        Function function1 = new Function();
        Function function2 = new Function();
        when(functionRepository.findAll()).thenReturn(List.of(function1, function2));

        List<Function> result = functionController.getAllFunctions();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void addFunction_shouldCreateAndReturnFunction() {
        FunctionDTO functionDTO = new FunctionDTO();
        Function function = new Function();
        function.setUid(UUID.randomUUID().toString());
        when(functionRepository.save(any(Function.class))).thenReturn(function);

        Function result = functionController.addFunction(functionDTO);

        assertNotNull(result);
        assertNotNull(result.getUid());
    }

    @Test
    void addFunction_shouldThrowExceptionWhenValidationFails() {
        FunctionDTO functionDTO = new FunctionDTO();
        doThrow(new InternalServiceException("Validation failed")).when(validator).validate(functionDTO);

        assertThrows(InternalServiceException.class, () -> functionController.addFunction(functionDTO));
    }

    @Test
    void updateFunction_shouldUpdateAndReturnFunction() {
        FunctionDTO functionDTO = new FunctionDTO();
        Function existingFunction = new Function();
        existingFunction.setUid("existingUid");
        existingFunction.setDeploymentState(DeploymentState.UNDEPLOYED);
        when(functionRepository.findByUid("existingUid")).thenReturn(existingFunction);
        when(functionRepository.save(any(Function.class))).thenReturn(existingFunction);

        functionController.updateFunction(functionDTO, "existingUid");

        verify(functionRepository, times(1)).save(any(Function.class));
    }

    @Test
    void updateFunction_shouldThrowExceptionWhenFunctionNotFound() {
        FunctionDTO functionDTO = new FunctionDTO();
        when(functionRepository.findByUid("nonExistingUid")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> functionController.updateFunction(functionDTO, "nonExistingUid"));
    }

    @Test
    void updateFunction_shouldThrowExceptionWhenFunctionIsDeploying() {
        FunctionDTO functionDTO = new FunctionDTO();
        Function existingFunction = new Function();
        existingFunction.setUid("existingUid");
        existingFunction.setDeploymentState(DeploymentState.DEPLOYING);
        when(functionRepository.findByUid("existingUid")).thenReturn(existingFunction);

        assertThrows(InternalServiceException.class, () -> functionController.updateFunction(functionDTO, "existingUid"));
    }

    @Test
    void deleteFunction_shouldDeleteFunctionWhenIdExists() {
        Function function = new Function();
        function.setUid("existingUid");
        when(functionRepository.findByUid("existingUid")).thenReturn(function);

        functionController.deleteFunction("existingUid");

        verify(functionRepository, times(1)).delete(function);
    }

    @Test
    void deleteFunction_shouldThrowExceptionWhenIdDoesNotExist() {
        when(functionRepository.findByUid("nonExistingUid")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> functionController.deleteFunction("nonExistingUid"));
    }

    @Test
    void deployFunction_shouldDeployFunctionWhenIdExists() {
        Function function = new Function();
        function.setUid("existingUid");
        when(functionRepository.findByUid("existingUid")).thenReturn(function);

        functionController.deployFunction("existingUid");

        verify(deploymentController, times(1)).deployFunctionImageToWorker(function);
    }

    @Test
    void unDeployFunction_shouldUndeployFunctionWhenIdExists() {
        Function function = new Function();
        function.setUid("existingUid");
        when(functionRepository.findByUid("existingUid")).thenReturn(function);

        functionController.unDeployFunction("existingUid");

        verify(deploymentController, times(1)).undeployFunction(function);
        verify(functionRepository, times(1)).save(function);
    }


    @Test
    void checkConnection_shouldCheckConnectionWhenClusterExists() {
        KubernetesCluster cluster = new KubernetesCluster();
        when(workerServiceClient.getKubernetesClusterById("existingUid")).thenReturn(cluster);

        functionController.checkConnection("existingUid");

        verify(deploymentController, times(1)).checkConnectionToKubernetes(cluster);
    }

    @Test
    void checkConnection_shouldThrowExceptionWhenClusterDoesNotExist() {
        when(workerServiceClient.getKubernetesClusterById("nonExistingUid")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> functionController.checkConnection("nonExistingUid"));
    }

    @Test
    void addJsonSchema_shouldCreateAndReturnJsonSchema() {
        JsonSchemaDTO jsonSchemaDTO = new JsonSchemaDTO();
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setUid(UUID.randomUUID().toString());
        when(jsonSchemaRepository.save(any(JsonSchema.class))).thenReturn(jsonSchema);

        JsonSchema result = functionController.addJsonSchema(jsonSchemaDTO);

        assertNotNull(result);
        assertNotNull(result.getUid());
    }

    @Test
    void getJsonSchemaList_shouldReturnListOfJsonSchemas() {
        JsonSchema jsonSchema1 = new JsonSchema();
        JsonSchema jsonSchema2 = new JsonSchema();
        when(jsonSchemaRepository.findAll()).thenReturn(List.of(jsonSchema1, jsonSchema2));

        List<JsonSchema> result = functionController.getJsonSchemaList();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getJsonSchema_shouldReturnJsonSchemaWhenIdExists() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setUid("existingUid");
        when(jsonSchemaRepository.findByUid("existingUid")).thenReturn(jsonSchema);

        JsonSchema result = functionController.getJsonSchema("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }


    @Test
    void deleteJsonSchema_shouldDeleteJsonSchemaWhenIdExists() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setUid("existingUid");
        when(jsonSchemaRepository.findByUid("existingUid")).thenReturn(jsonSchema);

        functionController.deleteJsonSchema("existingUid");

        verify(jsonSchemaRepository, times(1)).delete(jsonSchema);
    }

}