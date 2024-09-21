package de.lenneflow.workerservice.kubernetes.cloud;

import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.springframework.stereotype.Component;

@Component
public class AzureController implements  ICloudController{

    @Override
    public Object createCluster(KubernetesCluster kubernetesCluster) {
        return null;
    }

    @Override
    public Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName) {
        return null;
    }

    @Override
    public Object createNodeGroup(CloudNodeGroup cloudNodeGroup) {
        return null;
    }

    @Override
    public Object getCluster(KubernetesCluster kubernetesCluster) {
        return null;
    }

    @Override
    public Object getNodeGroup(CloudNodeGroup cloudNodeGroup) {
        return null;
    }
}
