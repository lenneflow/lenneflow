package de.lenneflow.functionservice.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Function service is working!";
    }
}
