package de.lenneflow.workerservice.feignclients;


import de.lenneflow.workerservice.feignmodel.Function;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "function-service", url = "http://localhost:47003")
//@FeignClient(name = "function-service")
public interface FunctionServiceClient {

    @GetMapping("/api/functions/{id}")
    public Function getFunctionById(@PathVariable String id);

    @PostMapping("/api/functions/{id}")
    public Function updateFunction(@RequestBody Function function, @PathVariable String id);

    @GetMapping("/api/functions/check")
    public String getFunctionHome();

    @GetMapping("/api/functions/all")
    public List<Function> getAllFunctions();
}
