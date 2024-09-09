package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.CloudCluster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CloudClusterRepository extends MongoRepository<CloudCluster, String> {

    CloudCluster findByUid(String uuid);

    CloudCluster findByClusterName(String name);

}
