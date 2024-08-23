package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    final
    FunctionRepository functionRepository;
    final Validator validator;
    final ModelMapper modelMapper;

    public FunctionController(FunctionRepository functionRepository, Validator validator) {
        this.functionRepository = functionRepository;
        this.validator = validator;
        modelMapper = new ModelMapper();
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Function Service! Everything is working fine!";
    }

    @GetMapping("/{uid}")
    public Function getFunctionById(@PathVariable String uid) {
        return functionRepository.findByUid(uid);
    }

    @GetMapping
    public Function getWorkerFunctionByName(@RequestParam(value = "name") String name) {
        return functionRepository.findByName(name);
    }

    @GetMapping("/all")
    public List<Function> getAllWorkerFunctions() {
        return functionRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function addWorkerFunction(@RequestBody FunctionDTO functionDTO) {
        Function function = modelMapper.map(functionDTO, Function.class);
        function.setUid(UUID.randomUUID().toString());
        validator.validateFunction(function);
        function.setCreationTime(LocalDateTime.now());
        function.setUpdateTime(LocalDateTime.now());
        return functionRepository.save(function);
    }

    @PostMapping("/{id}")
    public void updateWorkerFunction(@RequestBody Function function, @PathVariable String id) {
        //Function mapped = modelMapper.map(functionDTO, Function.class);
        //Function function = functionRepository.findByUid(id);
        //modelMapper.map(mapped, function);
        if(function == null) {
            throw new ResourceNotFoundException("Function not found");
        }
        function.setUpdateTime(LocalDateTime.now());
        functionRepository.save(function);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkerFunction(@PathVariable String id) {
        Function function = functionRepository.findByUid(id);
        if (function == null) {
           throw new ResourceNotFoundException("Function not found");
        }
        functionRepository.delete(function);
    }
}
