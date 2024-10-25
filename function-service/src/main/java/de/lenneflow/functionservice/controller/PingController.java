package de.lenneflow.functionservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Basic Controller that contains methods to check that the application is reachable
 * @author Idrissa Ganemtore
 */
@RestController
public class PingController {

    @Hidden
    @GetMapping(value={ "/ping"})
    public String checkService() {
        return "Welcome to the Function Service! Everything is working fine!";
    }

}
