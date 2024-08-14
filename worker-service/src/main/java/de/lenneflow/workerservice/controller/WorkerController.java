package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    final
    WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @PostMapping
    public ResponseEntity<Worker> createNewWorkerNode(@RequestBody Worker worker) {
        worker.setUuid(UUID.randomUUID().toString());
        return new ResponseEntity<>(workerRepository.save(worker), HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<Worker> updateWorkerNode(@RequestBody Worker worker) {
        return new ResponseEntity<>(workerRepository.save(worker), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerNode(@PathVariable String id) {
        return new ResponseEntity<>(workerRepository.findByUuid(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Worker> deleteWorkerNode(@PathVariable String id) {
        workerRepository.delete(workerRepository.findByUuid(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
