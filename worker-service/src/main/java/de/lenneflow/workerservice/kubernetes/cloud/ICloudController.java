package de.lenneflow.workerservice.kubernetes.cloud;

import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;

public interface ICloudController {

    Object createCluster(KubernetesCluster kubernetesCluster);

    Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName);

    Object createNodeGroup(ClusterNodeGroup clusterNodeGroup);

    Object getCluster(KubernetesCluster kubernetesCluster);

    Object getNodeGroup(ClusterNodeGroup clusterNodeGroup);
}
