package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.model.WorkerNode;
import de.lenneflow.workerservice.repository.WorkerNodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/worker_node")
public class WorkerNodeController {

    final
    WorkerNodeRepository workerNodeRepository;

    public WorkerNodeController(WorkerNodeRepository workerNodeRepository) {
        this.workerNodeRepository = workerNodeRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<WorkerNode> createNewWorkerNode(@RequestBody WorkerNode workerNode) {
        workerNode.setUuid(UUID.randomUUID().toString());
        return new ResponseEntity<>(workerNodeRepository.save(workerNode), HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<WorkerNode> updateWorkerNode(@RequestBody WorkerNode workerNode) {
        return new ResponseEntity<>(workerNodeRepository.save(workerNode), HttpStatus.OK);
    }

    @GetMapping("/get/{workerId}")
    public ResponseEntity<WorkerNode> getWorkerNode(@PathVariable String workerId) {
        return new ResponseEntity<>(workerNodeRepository.findByUuid(workerId), HttpStatus.OK);
    }

    @GetMapping("/delete/{workerId}")
    public ResponseEntity<WorkerNode> deleteWorkerNode(@PathVariable String workerId) {
        workerNodeRepository.delete(workerNodeRepository.findByUuid(workerId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
