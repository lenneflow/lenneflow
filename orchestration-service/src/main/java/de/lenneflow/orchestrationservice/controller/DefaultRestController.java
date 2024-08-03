package de.lenneflow.orchestrationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Orchestration service is working!";
    }
}
