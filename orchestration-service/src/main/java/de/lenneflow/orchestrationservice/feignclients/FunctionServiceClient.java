package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Function;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "function-service")
public interface FunctionServiceClient {

    @GetMapping("/api/functions/{uid}")
    public Function getFunctionByUid(@PathVariable String uid);

    @GetMapping("/api/functions/check")
    public String getFunctionHome();

    @GetMapping("/api/functions/list")
    public List<Function> getAllFunctions();
}
