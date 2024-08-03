package de.lenneflow.workerservice.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Worker service is working!";
    }
}
