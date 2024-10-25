package de.lenneflow.functionservice.util;


import de.lenneflow.functionservice.enums.CloudProvider;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *Class that creates YAML deployment and services files to be use by kubernetes.
 * @author Idrissa Ganemtore
 */
public class YamlEditor {

    private YamlEditor(){}

    /**
     * Creates a deployment resource
     * @param function function to deploy
     * @param replica the replica count
     * @param serviceAccountName the service account to use
     * @return the {@link Deployment} resource
     */
    public static Deployment createKubernetesDeploymentResource(Function function, int replica, String serviceAccountName) {
        return new DeploymentBuilder().withApiVersion("apps/v1").withKind("Deployment")
                .withNewMetadata().withName(function.getName()).endMetadata().withNewSpec().withReplicas(replica).withNewSelector()
                .withMatchLabels(Collections.singletonMap("app", function.getName())).endSelector().withNewTemplate().withNewMetadata().withLabels(Collections.singletonMap("app", function.getName())).endMetadata().withNewSpec().withServiceAccountName(serviceAccountName).withHostNetwork(false)
                .withContainers().addNewContainer().withName(function.getName()).withImage(function.getImageName()).withPorts().addNewPort().withContainerPort(function.getServicePort())
                .withHostPort(function.getAssignedHostPort()).endPort().endContainer().endSpec().endTemplate().endSpec().build();
    }


    /**
     * Creates a service resource
     * @param function function to deploy
     * @param cloudProvider the cloud provider
     * @return the {@link Service} resource
     */
    public static Service createKubernetesServiceResource(Function function, CloudProvider cloudProvider) {
        String serviceType = cloudProvider == CloudProvider.LOCAL ? "NodePort" : "LoadBalancer";
        return new ServiceBuilder().withApiVersion("v1").withKind("Service").withNewMetadata().withName(function.getName()).endMetadata().withNewSpec()
                .withType(serviceType).withSelector(Collections.singletonMap("app", function.getName())).withPorts().addNewPort().withPort(function.getAssignedHostPort()).withTargetPort(new IntOrString(function.getServicePort())).endPort()
                .endSpec().build();
    }

    /**
     * Creates a service account resource
     * @param serviceAccountName the name of the account
     * @return the {@link ServiceAccount} resource
     */
    public static ServiceAccount createKubernetesServiceAccountResource(String serviceAccountName) {
        return new ServiceAccountBuilder().withApiVersion("v1").withKind("ServiceAccount").withNewMetadata().withName(serviceAccountName)
                .endMetadata().build();
    }

    /**
     * Creates a cluster role resource
     * @param serviceAccountName the name of the account
     * @param namespace the namespace to use
     * @return the {@link ClusterRole} resource
     */
    public static ClusterRole createKubernetesClusterRoleResource(String serviceAccountName, String namespace) {
        return new ClusterRoleBuilder().withKind("ClusterRole").withApiVersion("rbac.authorization.k8s.io/v1").withNewMetadata().withName(serviceAccountName).withNamespace(namespace)
                .endMetadata().withRules().addNewRule().withApiGroups(Arrays.asList("")).withResources(Arrays.asList("configmaps", "pods", "services", "endpoints", "secrets"))
                .withVerbs(Arrays.asList("get", "list", "watch")).endRule().build();
    }

    /**
     * Creates a role binding resource
     * @param serviceAccountName the name of the account
     * @param namespace the namespace to use
     * @return the {@link ClusterRoleBinding} object
     */
    public static ClusterRoleBinding createKubernetesClusterRoleBindingResource(String serviceAccountName, String namespace) {
        return new ClusterRoleBindingBuilder().withKind("ClusterRoleBinding").withApiVersion("rbac.authorization.k8s.io/v1").withNewMetadata().withName(serviceAccountName)
                .withNamespace(namespace).endMetadata().withSubjects().addNewSubject().withKind("ServiceAccount").withName(serviceAccountName).withNamespace(namespace)
                .endSubject().withNewRoleRef().withApiGroup("rbac.authorization.k8s.io").withKind("ClusterRole").withName(serviceAccountName).endRoleRef().build();
    }

    /**
     * Creates a ingress resource.
     * @param kubernetesCluster the cluster
     * @param function the function to target
     * @return the {@link Ingress} resource
     */
    public static Ingress createKubernetesIngressResource(KubernetesCluster kubernetesCluster, Function function) {
        String ingressName = kubernetesCluster.getClusterName().toLowerCase() + "-ingress";
        Map<String, String> ingressAnnotations = new HashMap<>();
        ingressAnnotations.put("kubernetes.io/ingress.class", "nginx");
        ingressAnnotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
        return new IngressBuilder().withApiVersion("networking.k8s.io/v1").withKind("Ingress").withNewMetadata().withName(ingressName).withAnnotations(ingressAnnotations).endMetadata().withNewSpec()
                .withIngressClassName("nginx").withRules().addNewRule().withHost(kubernetesCluster.getHostAddress()).withNewHttp().withPaths().addNewPath().withPath(function.getResourcePath())
                .withPathType("Prefix").withNewBackend().withNewService().withName(function.getName()).withNewPort().withNumber(function.getAssignedHostPort()).endPort().endService().endBackend()
                .endPath().endHttp().endRule().endSpec().build();
    }

    /**
     * Adds a new path to an existing ingress
     * @param ingress the ingress to update
     * @param function the function to target
     * @return the {@link Ingress} resource
     */
    public static Ingress addPathToKubernetesIngressResource(Ingress ingress, Function function) {
        ingress.getSpec().getRules().get(0).getHttp().getPaths().add(new HTTPIngressPathBuilder().withPath(function.getResourcePath())
                .withPathType("Prefix").withNewBackend().withNewService().withName(function.getName()).withNewPort().withNumber(function.getAssignedHostPort()).endPort().endService().endBackend().build());
        return ingress;
    }


}
