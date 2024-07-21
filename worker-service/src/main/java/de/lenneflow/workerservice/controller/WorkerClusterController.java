package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.model.WorkerCluster;
import de.lenneflow.workerservice.repository.WorkerClusterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/worker_cluster")
public class WorkerClusterController {

    final
    WorkerClusterRepository workerClusterRepository;

    public WorkerClusterController(WorkerClusterRepository workerClusterRepository) {
        this.workerClusterRepository = workerClusterRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<WorkerCluster> createNewCluster(@RequestBody WorkerCluster workerCluster) {
        workerCluster.setUuid(UUID.randomUUID().toString());
        return new ResponseEntity<>(workerClusterRepository.save(workerCluster), HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<WorkerCluster> updateCluster(@RequestBody WorkerCluster workerCluster) {
        return new ResponseEntity<>(workerClusterRepository.save(workerCluster), HttpStatus.OK);
    }

    @GetMapping("/get/{workerId}")
    public ResponseEntity<WorkerCluster> getCluster(@PathVariable String workerId) {
        return new ResponseEntity<>(workerClusterRepository.findByUuid(workerId), HttpStatus.OK);
    }

    @GetMapping("/delete/{workerId}")
    public ResponseEntity<WorkerCluster> deleteCluster(@PathVariable String workerId) {
        workerClusterRepository.delete(workerClusterRepository.findByUuid(workerId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
