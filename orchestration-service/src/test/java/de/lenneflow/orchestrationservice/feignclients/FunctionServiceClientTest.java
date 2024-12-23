package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FunctionServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private FunctionServiceClient functionServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        functionServiceClient = new FunctionServiceClientImpl(restTemplate);
    }

    @Test
    void getFunctionByUid_shouldReturnFunction() {
        String uid = "functionUid";
        Function function = new Function();
        when(restTemplate.getForObject("/api/functions/" + uid, Function.class)).thenReturn(function);

        Function result = functionServiceClient.getFunctionByUid(uid);

        assertNotNull(result);
        assertEquals(function, result);
    }

    @Test
    void getFunctionHome_shouldReturnHomeString() {
        String homeString = "home";
        when(restTemplate.getForObject("/api/functions/ping", String.class)).thenReturn(homeString);

        String result = functionServiceClient.getFunctionHome();

        assertNotNull(result);
        assertEquals(homeString, result);
    }

    @Test
    void getAllFunctions_shouldReturnFunctionList() {
        List<Function> functions = List.of(new Function(), new Function());
        when(restTemplate.getForObject("/api/functions/list", List.class)).thenReturn(functions);

        List<Function> result = functionServiceClient.getAllFunctions();

        assertNotNull(result);
        assertEquals(functions.size(), result.size());
    }

    @Test
    void deployFunction_shouldDeployFunction() {
        String functionId = "functionId";
        when(restTemplate.getForObject("/api/functions/" + functionId + "/deploy", Void.class)).thenReturn(null);

        functionServiceClient.deployFunction(functionId);

        verify(restTemplate, times(1)).getForObject("/api/functions/" + functionId + "/deploy", Void.class);
    }

    // Inner class to implement the FunctionServiceClient interface
    private static class FunctionServiceClientImpl implements FunctionServiceClient {
        private final RestTemplate restTemplate;

        public FunctionServiceClientImpl(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @Override
        public Function getFunctionByUid(String uid) {
            return restTemplate.getForObject("/api/functions/" + uid, Function.class);
        }

        @Override
        public String getFunctionHome() {
            return restTemplate.getForObject("/api/functions/ping", String.class);
        }

        @Override
        public List<Function> getAllFunctions() {
            return restTemplate.getForObject("/api/functions/list", List.class);
        }

        @Override
        public void deployFunction(String functionId) {
            restTemplate.getForObject("/api/functions/" + functionId + "/deploy", Void.class);
        }
    }
}