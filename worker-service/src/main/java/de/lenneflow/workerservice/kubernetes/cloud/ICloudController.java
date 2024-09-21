package de.lenneflow.workerservice.kubernetes.cloud;

import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;

public interface ICloudController {

    Object createCluster(KubernetesCluster kubernetesCluster);

    Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName);

    Object createNodeGroup(CloudNodeGroup cloudNodeGroup);

    Object getCluster(KubernetesCluster kubernetesCluster);

    Object getNodeGroup(CloudNodeGroup cloudNodeGroup);
}
