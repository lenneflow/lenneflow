package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.WorkerDTO;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import de.lenneflow.workerservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    final
    WorkerRepository workerRepository;
    final ModelMapper modelMapper;
    final Validator validator;

    public WorkerController(WorkerRepository workerRepository, Validator validator) {
        this.workerRepository = workerRepository;
        this.validator = validator;
        modelMapper = new ModelMapper();
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @PostMapping
    public ResponseEntity<WorkerDTO> createNewWorker(@RequestBody WorkerDTO workerDTO) {
        Worker worker = modelMapper.map(workerDTO, Worker.class);
        worker.setUid(UUID.randomUUID().toString());
        worker.setCreated(LocalDateTime.now());
        worker.setUpdated(LocalDateTime.now());
        validator.validate(worker);
        Worker savedWorker = workerRepository.save(worker);
        return new ResponseEntity<>(modelMapper.map(savedWorker, WorkerDTO.class), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkerDTO> updateWorkerNode(@RequestBody WorkerDTO workerDTO, @PathVariable String id) {
        Worker worker = workerRepository.findByUid(id);
        modelMapper.map(workerDTO, worker);
        if(worker == null) {
            throw new ResourceNotFoundException("Worker not found");
        }
        validator.validate(worker);
        Worker savedWorker = workerRepository.save(worker);
        return new ResponseEntity<>(modelMapper.map(savedWorker, WorkerDTO.class), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerDTO> getWorkerNode(@PathVariable String id) {
        Worker foundWorker = workerRepository.findByUid(id);
        if(foundWorker == null) {
            throw new ResourceNotFoundException("Worker not found");
        }
        return new ResponseEntity<>(modelMapper.map(foundWorker, WorkerDTO.class), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Worker> deleteWorkerNode(@PathVariable String id) {
        Worker foundWorker = workerRepository.findByUid(id);
        if(foundWorker == null) {
            throw new ResourceNotFoundException("Worker with id " + id + " not found");
        }
        workerRepository.delete(foundWorker);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
