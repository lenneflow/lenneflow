package de.lenneflow.workerservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Basic Controller that contains methods to check that the application is reachable
 */
@RestController
@RequestMapping("/api/worker")
public class PingController {

    @Hidden
    @GetMapping(value={ "/ping"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

}
