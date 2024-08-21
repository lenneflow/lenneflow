package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.WorkerDTO;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import de.lenneflow.workerservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    final
    WorkerRepository workerRepository;
    final ModelMapper modelMapper;
    final Validator validator;
    final FunctionServiceClient functionServiceClient;

    public WorkerController(WorkerRepository workerRepository, Validator validator, FunctionServiceClient functionServiceClient) {
        this.workerRepository = workerRepository;
        this.validator = validator;
        this.functionServiceClient = functionServiceClient;
        modelMapper = new ModelMapper();
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @PostMapping
    public ResponseEntity<Worker> createNewWorker(@RequestBody WorkerDTO workerDTO) {
        Worker worker = modelMapper.map(workerDTO, Worker.class);
        worker.setUid(UUID.randomUUID().toString());
        worker.setCreated(LocalDateTime.now());
        worker.setUpdated(LocalDateTime.now());
        validator.validate(worker);
        Worker savedWorker = workerRepository.save(worker);
        return new ResponseEntity<>(savedWorker, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Worker> updateWorker(@RequestBody WorkerDTO workerDTO, @PathVariable String id) {
        Worker worker = workerRepository.findByUid(id);
        modelMapper.map(workerDTO, worker);
        if(worker == null) {
            throw new ResourceNotFoundException("Worker not found");
        }
        validator.validate(worker);
        Worker savedWorker = workerRepository.save(worker);
        return new ResponseEntity<>(savedWorker, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorker(@PathVariable String id) {
        Worker foundWorker = workerRepository.findByUid(id);
        if(foundWorker == null) {
            throw new ResourceNotFoundException("Worker not found");
        }
        return new ResponseEntity<>(foundWorker, HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Worker> deleteWorker(@PathVariable String id) {
        Worker foundWorker = workerRepository.findByUid(id);
        if(foundWorker == null) {
            throw new ResourceNotFoundException("Worker with id " + id + " not found");
        }
        workerRepository.delete(foundWorker);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
