package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Function;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * This is the feign client class for the function service. This class acts as the interface between the orchestration service and the
 * function service. It will make rest calls to the function service and return the responses.
 *
 * @author Idrissa Ganemtore
 */
@FeignClient(name = "function-service")
public interface FunctionServiceClient {

    @GetMapping("/api/functions/{uid}")
    Function getFunctionByUid(@PathVariable String uid);

    @GetMapping("/api/functions/ping")
    String getFunctionHome();

    @GetMapping("/api/functions/list")
    List<Function> getAllFunctions();

    @GetMapping("/api/functions/{uid}/deploy")
    void deployFunction(@PathVariable("uid") String functionId);
}
