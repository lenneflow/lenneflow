package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KubernetesClusterRepository extends MongoRepository<KubernetesCluster, String> {

    KubernetesCluster findByUid(String uuid);

    KubernetesCluster findByClusterNameAndCloudProviderAndRegion(String clusterName, CloudProvider cloudProvider, String region);

    List<KubernetesCluster> findBySupportedFunctionTypesContaining(String functionType);

}
