package de.lenneflow.workflowservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Workflow service is working!";
    }
}
