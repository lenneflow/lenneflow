package de.lenneflow.workerservice.repository;


import de.lenneflow.workerservice.model.AccessToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessTokenRepository extends MongoRepository<AccessToken, String> {

    AccessToken findByUid(String uuid);

}
