package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.ApiCredential;
import de.lenneflow.workerservice.model.CloudCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiCredentialRepository extends MongoRepository<ApiCredential, String> {

    ApiCredential findByUid(String uuid);

}
