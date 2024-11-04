package de.lenneflow.functionservice.controller;

import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Basic Controller that contains methods to check that the application is reachable
 * @author Idrissa Ganemtore
 */
@RestController
@RequestMapping("/api/function")
public class PingController {

    final WorkerServiceClient workerServiceClient;

    public PingController(WorkerServiceClient workerServiceClient) {
        this.workerServiceClient = workerServiceClient;
    }

    @Hidden
    @GetMapping(value={ "/ping"})
    public String checkService() {
        return "Welcome to the Function Service! Everything is working fine!";
    }

    @Hidden
    @GetMapping(value={ "/ping/feign"})
    public String checkFeignService() {
        return workerServiceClient.ping();
    }

}
