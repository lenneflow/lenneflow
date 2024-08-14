package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.model.WorkerCluster;
import de.lenneflow.workerservice.repository.WorkerClusterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workers/clusters")
public class WorkerClusterController {

    final
    WorkerClusterRepository workerClusterRepository;

    public WorkerClusterController(WorkerClusterRepository workerClusterRepository) {
        this.workerClusterRepository = workerClusterRepository;
    }

    @PostMapping
    public ResponseEntity<WorkerCluster> createNewCluster(@RequestBody WorkerCluster workerCluster) {
        workerCluster.setUuid(UUID.randomUUID().toString());
        return new ResponseEntity<>(workerClusterRepository.save(workerCluster), HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<WorkerCluster> updateCluster(@RequestBody WorkerCluster workerCluster) {
        return new ResponseEntity<>(workerClusterRepository.save(workerCluster), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerCluster> getCluster(@PathVariable String id) {
        return new ResponseEntity<>(workerClusterRepository.findByUuid(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkerCluster> deleteCluster(@PathVariable String id) {
        workerClusterRepository.delete(workerClusterRepository.findByUuid(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
