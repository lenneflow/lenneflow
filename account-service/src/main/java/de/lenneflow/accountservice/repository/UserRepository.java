package de.lenneflow.accountservice.repository;

import de.lenneflow.accountservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUid(String uid);
}
