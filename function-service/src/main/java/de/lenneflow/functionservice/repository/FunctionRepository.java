package de.lenneflow.functionservice.repository;


import de.lenneflow.functionservice.enums.FunctionStatus;
import de.lenneflow.functionservice.model.Function;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FunctionRepository extends MongoRepository<Function, String> {

    Function findByUid(String Uid);

    Function findByFunctionName(String functionName);

    List<Function> findByFunctionStatus(FunctionStatus status);

    List<Function> findByFunctionType(String functionType);

    List<Function> findByFunctionPriority(int priority);

}
