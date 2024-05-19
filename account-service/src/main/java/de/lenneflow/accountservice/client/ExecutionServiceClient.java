package de.lenneflow.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "execution-service")
public interface ExecutionServiceClient {

    @GetMapping("/execution/hello")
    public ResponseEntity<String> getHello();

}
