package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.CloudCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CloudCredentialRepository extends MongoRepository<CloudCredential, String> {

    CloudCredential findByUid(String uuid);

    CloudCredential findByName(String name);

}
