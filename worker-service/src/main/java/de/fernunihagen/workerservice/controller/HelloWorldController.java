package de.fernunihagen.workerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worker")
public class HelloWorldController {

    @GetMapping("/hello")
    public ResponseEntity<String> getAnonymous() {
        return ResponseEntity.ok("Welcome to worker service");
    }
}
