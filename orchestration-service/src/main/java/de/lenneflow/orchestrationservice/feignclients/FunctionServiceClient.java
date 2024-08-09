package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Function;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "function-service")
public interface FunctionServiceClient {

    @GetMapping("/api/function/get/{uuid}")
    public Function getFunction(@PathVariable String uuid);

    @GetMapping("/api/function")
    public String getFunctionHome();
}
