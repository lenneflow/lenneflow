package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.WorkerNode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerNodeRepository extends MongoRepository<WorkerNode, String> {

    WorkerNode findByUuid(String uuid);

    WorkerNode findByName(String name);

    List<WorkerNode> findByClusterUuid(String clusterId);

    WorkerNode findByIpAddress(String ipAddress);

    WorkerNode findByHostName(String hostName);
}
