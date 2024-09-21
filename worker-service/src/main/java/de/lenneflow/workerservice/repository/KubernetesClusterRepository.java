package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.KubernetesCluster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KubernetesClusterRepository extends MongoRepository<KubernetesCluster, String> {

    KubernetesCluster findByUid(String uuid);

    KubernetesCluster findByClusterName(String name);

    List<KubernetesCluster> findBySupportedFunctionTypesContaining(String functionType);

}
