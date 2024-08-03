package de.lenneflow.functionservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultRestController {

    @GetMapping("/")
    public String home() {
        return "Function service is working!";
    }
}
