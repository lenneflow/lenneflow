package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerRepository extends MongoRepository<Worker, String> {

    Worker findByUid(String uuid);

    Worker findByName(String name);

    Worker findByIpAddress(String ipAddress);

    Worker findByHostName(String hostName);

    List<Worker> findBySupportedFunctionTypesContaining(String functionType);
}
