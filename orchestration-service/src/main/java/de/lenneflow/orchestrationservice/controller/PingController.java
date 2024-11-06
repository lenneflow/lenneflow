package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Basic Controller that contains methods to check that the application is reachable
 */
@RestController
@RequestMapping("/api/control")
@Hidden
public class PingController {


    final FunctionServiceClient functionServiceClient;

    public PingController(FunctionServiceClient functionServiceClient) {
        this.functionServiceClient = functionServiceClient;
    }

    @Hidden
    @GetMapping(value={"/ping"})
    public String checkService() {
        return "Welcome to the Orchestration Service!";
    }

    @Hidden
    @GetMapping(value={"/ping-function"})
    public String checkFeign() {
        return functionServiceClient.getFunctionHome();
    }
}
