package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.ClusterNodeGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClusterNodeGroupRepository extends MongoRepository<ClusterNodeGroup, String> {

    ClusterNodeGroup findByUid(String uuid);

    ClusterNodeGroup findByGroupName(String name);

}
