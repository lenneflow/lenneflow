package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.WorkerCluster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkerClusterRepository extends MongoRepository<WorkerCluster, String> {

    WorkerCluster findByUuid(String uuid);

    WorkerCluster findByName(String name);

    WorkerCluster findByIpAddress(String ipAddress);

    WorkerCluster findByHostName(String hostName);
}
