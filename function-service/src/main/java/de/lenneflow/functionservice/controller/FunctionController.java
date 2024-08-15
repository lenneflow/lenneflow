package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    final
    FunctionRepository functionRepository;

    public FunctionController(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Function Service! Everything is working fine!";
    }

    @GetMapping("/{id}")
    public Function getFunctionById(@PathVariable String id) {
        return functionRepository.findById(id).orElse(null);
    }

    @GetMapping
    public Function getWorkerFunctionByName(@RequestParam(value = "name") String name) {
        return functionRepository.findByFunctionName(name);
    }

    @GetMapping
    public List<Function> getAllWorkerFunctions() {
        return functionRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function addWorkerFunction(@RequestBody Function function) {
        function.setUid(UUID.randomUUID().toString());
        return functionRepository.save(function);
    }

    @PatchMapping
    public void updateWorkerFunction(@RequestBody Function function) {
        functionRepository.save(function);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkerFunction(@PathVariable String id) {
        Function function = functionRepository.findByUid(id);
        if (function != null) {
            functionRepository.delete(function);
        }
    }
}
