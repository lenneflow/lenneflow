package de.lenneflow.orchestrationservice.repository;


import de.lenneflow.orchestrationservice.model.GlobalInputData;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for the global input data entity
 *
 * @author Idrissa Ganemtore
 */
public interface GlobalInputDataRepository extends MongoRepository<GlobalInputData, String> {

    GlobalInputData findByUid(String uid);
}
