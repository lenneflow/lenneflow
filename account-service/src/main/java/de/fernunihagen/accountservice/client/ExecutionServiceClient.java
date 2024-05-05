package de.fernunihagen.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "execution-service")
@RequestMapping("/execution")
public interface ExecutionServiceClient {

    @GetMapping("/hello")
    public ResponseEntity<String> getHello();

}
