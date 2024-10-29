package de.lenneflow.functionservice.repository;


import de.lenneflow.functionservice.model.JsonSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JsonSchemaRepository extends MongoRepository<JsonSchema, String> {

    JsonSchema findByUid(String uid);

}
