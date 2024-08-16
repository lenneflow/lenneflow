package de.lenneflow.functionservice.repository;


import de.lenneflow.functionservice.model.Function;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FunctionRepository extends MongoRepository<Function, String> {

    Function findByUid(String Uid);

    Function findByName(String functionName);

    List<Function> findByType(String functionType);


}
