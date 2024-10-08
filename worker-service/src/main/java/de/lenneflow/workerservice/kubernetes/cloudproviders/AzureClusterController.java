package de.lenneflow.workerservice.kubernetes.cloudproviders;

import de.lenneflow.workerservice.kubernetes.IClusterController;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AzureClusterController implements IClusterController {

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

    @Override
    public String getSessionToken(KubernetesCluster kubernetesCluster, Date expirationDate) {
        return null;
    }

    @Override
    public String getApiServerEndpoint(KubernetesCluster kubernetesCluster) {
        return "";
    }
}
