package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.LocalCluster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerRepository extends MongoRepository<LocalCluster, String> {

    LocalCluster findByUid(String uuid);

    LocalCluster findByName(String name);

    LocalCluster findByIpAddress(String ipAddress);

    LocalCluster findByHostName(String hostName);

    List<LocalCluster> findBySupportedFunctionTypesContaining(String functionType);
}
