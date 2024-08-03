package de.lenneflow.workflowservice.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Workflow service is working!";
    }
}
