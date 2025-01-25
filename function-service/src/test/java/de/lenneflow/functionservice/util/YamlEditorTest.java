package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.enums.CloudProvider;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlEditorTest {

    @Test
    void createKubernetesDeploymentResource_shouldCreateDeploymentWithCorrectValues() {
        Function function = new Function();
        function.setName("testFunction");
        function.setImageName("testImage");
        function.setServicePort(8080);
        function.setCpuRequest("500m");
        function.setMemoryRequest("256Mi");

        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function, 1, "testServiceAccount");

        assertEquals("testFunction", deployment.getMetadata().getName());
        assertEquals("testImage", deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        assertEquals(8080, deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort());
    }

    @Test
    void createKubernetesServiceResource_shouldCreateServiceWithCorrectValues() {
        Function function = new Function();
        function.setName("testFunction");
        function.setAssignedHostPort(30000);
        function.setServicePort(8080);

        Service service = YamlEditor.createKubernetesServiceResource(function, CloudProvider.LOCAL);

        assertEquals("testFunction", service.getMetadata().getName());
        assertEquals("ClusterIP", service.getSpec().getType());
        assertEquals(30000, service.getSpec().getPorts().get(0).getPort());
        assertEquals(8080, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
    }

    @Test
    void createKubernetesServiceAccountResource_shouldCreateServiceAccountWithCorrectValues() {
        ServiceAccount serviceAccount = YamlEditor.createKubernetesServiceAccountResource("testServiceAccount");

        assertEquals("testServiceAccount", serviceAccount.getMetadata().getName());
    }

    @Test
    void createKubernetesClusterRoleResource_shouldCreateClusterRoleWithCorrectValues() {
        ClusterRole clusterRole = YamlEditor.createKubernetesClusterRoleResource("testServiceAccount", "testNamespace");

        assertEquals("testServiceAccount", clusterRole.getMetadata().getName());
        assertEquals("testNamespace", clusterRole.getMetadata().getNamespace());
    }

    @Test
    void createKubernetesClusterRoleBindingResource_shouldCreateClusterRoleBindingWithCorrectValues() {
        ClusterRoleBinding clusterRoleBinding = YamlEditor.createKubernetesClusterRoleBindingResource("testServiceAccount", "testNamespace");

        assertEquals("testServiceAccount", clusterRoleBinding.getMetadata().getName());
        assertEquals("testNamespace", clusterRoleBinding.getMetadata().getNamespace());
    }

    @Test
    void createKubernetesIngressResource_shouldCreateIngressWithCorrectValues() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setIngressServiceName("testIngress");
        kubernetesCluster.setHostUrl("http://localhost");

        Function function = new Function();
        function.setName("testFunction");
        function.setResourcePath("/testPath");
        function.setAssignedHostPort(30000);

        Ingress ingress = YamlEditor.createKubernetesIngressResource(kubernetesCluster, function);

        assertEquals("testIngress", ingress.getMetadata().getName());
        assertEquals("localhost", ingress.getSpec().getRules().get(0).getHost());
        assertEquals("/testPath", ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
    }

    @Test
    void addPathToKubernetesIngressResource_shouldAddPathToIngress() {
        Ingress ingress = new IngressBuilder().withNewSpec().withRules().addNewRule().withNewHttp().withPaths().endHttp().endRule().endSpec().build();
        Function function = new Function();
        function.setName("testFunction");
        function.setResourcePath("/newPath");
        function.setAssignedHostPort(30000);

        Ingress updatedIngress = YamlEditor.addPathToKubernetesIngressResource(ingress, function);

        assertEquals("/newPath", updatedIngress.getSpec().getRules().get(0).getHttp().getPaths().get(0).getPath());
    }

    @Test
    void createResourcesRequestMap_shouldCreateCorrectRequestMap() {
        Function function = new Function();
        function.setCpuRequest("500m");
        function.setMemoryRequest("256Mi");

        Map<String, Quantity> reqMap = YamlEditor.createResourcesRequestMap(function);

        assertEquals("500", reqMap.get("cpu").getAmount());
        assertEquals("m", reqMap.get("cpu").getFormat());
        assertEquals("256", reqMap.get("memory").getAmount());
        assertEquals("Mi", reqMap.get("memory").getFormat());
    }
}