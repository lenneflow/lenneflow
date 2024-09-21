package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.CloudNodeGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CloudNodeGroupRepository extends MongoRepository<CloudNodeGroup, String> {

    CloudNodeGroup findByUid(String uuid);

    CloudNodeGroup findByGroupName(String name);

}
