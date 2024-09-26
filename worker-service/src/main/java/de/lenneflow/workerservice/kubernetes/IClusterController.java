package de.lenneflow.workerservice.kubernetes;

import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;

public interface IClusterController {

    Object createCluster(KubernetesCluster kubernetesCluster);

    Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName);

    Object createNodeGroup(ClusterNodeGroup clusterNodeGroup);

    Object getCluster(KubernetesCluster kubernetesCluster);

    Object getNodeGroup(ClusterNodeGroup clusterNodeGroup);

    String getSessionToken(KubernetesCluster kubernetesCluster);
}
