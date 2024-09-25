package de.lenneflow.workerservice.kubernetes.cloud;

import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.springframework.stereotype.Component;

@Component
public class GoogleController implements ICloudController{

    @Override
    public Object createCluster(KubernetesCluster kubernetesCluster) {
        return null;
    }

    @Override
    public Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName) {
        return null;
    }

    @Override
    public Object createNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        return null;
    }

    @Override
    public Object getCluster(KubernetesCluster kubernetesCluster) {
        return null;
    }

    @Override
    public Object getNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        return null;
    }
}
