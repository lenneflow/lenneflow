package de.fernunihagen.accountservice.controller;

import de.fernunihagen.accountservice.client.ExecutionServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class HelloWorldController {

    final
    ExecutionServiceClient executionClient;

    public HelloWorldController(ExecutionServiceClient executionClient) {
        this.executionClient = executionClient;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("Welcome to account service");
    }

    @GetMapping("/hello2")
    public ResponseEntity<String> getHello2() {
        return  executionClient.getHello();
    }
}
