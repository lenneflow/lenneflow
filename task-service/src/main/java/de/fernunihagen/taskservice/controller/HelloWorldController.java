package de.fernunihagen.taskservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class HelloWorldController {

    @GetMapping("/hello")
    public ResponseEntity<String> getAnonymous() {
        return ResponseEntity.ok("Welcome to task service");
    }
}
