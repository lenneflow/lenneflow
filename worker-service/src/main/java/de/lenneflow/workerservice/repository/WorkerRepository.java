package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerRepository  extends MongoRepository<Worker, String> {

    Worker findByUuid(String uuid);

    Worker findByName(String name);

    List<Worker> findByNameSpace(String nameSpace);
}
