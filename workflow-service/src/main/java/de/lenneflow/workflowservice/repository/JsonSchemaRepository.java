package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.JsonSchema;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface JsonSchemaRepository extends MongoRepository<JsonSchema, String> {

    JsonSchema findByUid(String uid);

}
