package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.KubernetesCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KubernetesCredentialRepository extends MongoRepository<KubernetesCredential, String> {

    KubernetesCredential findByUid(String uuid);

    KubernetesCredential findByIpAddress(String name);

}
