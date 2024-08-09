package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    final
    WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @GetMapping("/")
    public String checkService() {
        return "Worker service is working!";
    }

    @PostMapping("/create")
    public ResponseEntity<Worker> createNewWorkerNode(@RequestBody Worker worker) {
        worker.setUuid(UUID.randomUUID().toString());
        return new ResponseEntity<>(workerRepository.save(worker), HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<Worker> updateWorkerNode(@RequestBody Worker worker) {
        return new ResponseEntity<>(workerRepository.save(worker), HttpStatus.OK);
    }

    @GetMapping("/get/{workerId}")
    public ResponseEntity<Worker> getWorkerNode(@PathVariable String workerId) {
        return new ResponseEntity<>(workerRepository.findByUuid(workerId), HttpStatus.OK);
    }

    @GetMapping("/delete/{workerId}")
    public ResponseEntity<Worker> deleteWorkerNode(@PathVariable String workerId) {
        workerRepository.delete(workerRepository.findByUuid(workerId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
