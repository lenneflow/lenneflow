package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
//@RequestMapping("/function")
public class FunctionController {

    final
    FunctionRepository functionRepository;

    public FunctionController(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    @GetMapping("/")
    public String home() {
        return "Function service is working!";
    }

    @GetMapping("function/get/{id}")
    public Function getWorkerFunctionById(@PathVariable String id) {
        return functionRepository.findById(id).orElse(null);
    }

    @GetMapping("function/get/all")
    public List<Function> getAllWorkerFunctions() {
        return functionRepository.findAll();
    }

    @PostMapping("function/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Function addWorkerFunction(@RequestBody Function function) {
        function.setFunctionID(UUID.randomUUID().toString());
        return functionRepository.save(function);
    }

    @PatchMapping("function/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateWorkerFunction(@RequestBody Function function) {
        functionRepository.save(function);
    }
}
