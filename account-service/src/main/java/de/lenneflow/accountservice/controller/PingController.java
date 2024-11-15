package de.lenneflow.accountservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@Hidden
public class PingController {

    @Hidden
    @GetMapping("/ping")
    public ResponseEntity<String> checkService() {
        return  new ResponseEntity<>("Account service is working properly!", HttpStatus.OK);
    }

}
