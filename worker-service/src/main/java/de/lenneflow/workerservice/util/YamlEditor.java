package de.lenneflow.workerservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.Worker;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class YamlEditor {


    public static Deployment createKubernetesDeploymentResource(Function function, int replica, String serviceAccountName, int hostPort) {
        return new DeploymentBuilder().withApiVersion("apps/v1").withKind("Deployment")
                .withNewMetadata().withName(function.getName()).endMetadata().withNewSpec().withReplicas(replica).withNewSelector()
                .withMatchLabels(Collections.singletonMap("app", function.getName())).endSelector().withNewTemplate().withNewMetadata().withLabels(Collections.singletonMap("app", function.getName())).endMetadata().withNewSpec().withServiceAccountName(serviceAccountName).withHostNetwork(true)
                .withContainers().addNewContainer().withName(function.getName()).withImage(function.getImageName()).withPorts().addNewPort().withContainerPort(hostPort)
                .withHostPort(hostPort).endPort().endContainer().endSpec().endTemplate().endSpec().build();
    }

    public static Service createKubernetesServiceResource(Function function, String serviceType, int port) {
        return new ServiceBuilder().withApiVersion("v1").withKind("Service").withNewMetadata().withName(function.getName()).endMetadata().withNewSpec()
                .withType(serviceType).withSelector(Collections.singletonMap("app", function.getName())).withPorts().addNewPort().withPort(port).withTargetPort(new IntOrString(port)).endPort()
                .endSpec().build();
    }

    public static ServiceAccount createKubernetesServiceAccountResource(String serviceAccountName) {
        return new ServiceAccountBuilder().withApiVersion("v1").withKind("ServiceAccount").withNewMetadata().withName(serviceAccountName)
                .endMetadata().build();
    }

    public static ClusterRole createKubernetesClusterRoleResource(String serviceAccountName, String namespace) {
        return new ClusterRoleBuilder().withKind("ClusterRole").withApiVersion("rbac.authorization.k8s.io/v1").withNewMetadata().withName(serviceAccountName).withNamespace(namespace)
                .endMetadata().withRules().addNewRule().withApiGroups(new ArrayList<>()).withResources(Arrays.asList("configmaps", "pods", "services", "endpoints", "secrets"))
                .withVerbs(Arrays.asList("get", "list", "watch")).endRule().build();
    }

    public static ClusterRoleBinding createKubernetesClusterRoleBindingResource(String serviceAccountName, String namespace) {
        return new ClusterRoleBindingBuilder().withKind("ClusterRoleBinding").withApiVersion("rbac.authorization.k8s.io/v1").withNewMetadata().withName(serviceAccountName)
                .withNamespace(namespace).endMetadata().withSubjects().addNewSubject().withKind("ServiceAccount").withName(serviceAccountName).withNamespace(namespace)
                .endSubject().withNewRoleRef().withApiGroup("rbac.authorization.k8s.io").withKind("ClusterRole").withName(serviceAccountName).endRoleRef().build();
    }

    public static Ingress createKubernetesIngressResource(Worker worker, Function function, int port) {
        String ingressName = worker.getName() + "_ingress";
        Map<String, String> ingressAnnotations = new HashMap<>();
        ingressAnnotations.put("kubernetes.io/ingress.class", "nginx");
        ingressAnnotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
        return new IngressBuilder().withApiVersion("networking.k8s.io/v1").withKind("Ingress").withNewMetadata().withName(ingressName).withAnnotations(ingressAnnotations).endMetadata().withNewSpec()
                .withIngressClassName("nginx").withRules().addNewRule().withHost(worker.getHostName()).withNewHttp().withPaths().addNewPath().withPath(function.getResourcePath())
                .withPathType("Prefix").withNewBackend().withServiceName(function.getName()).withServicePort(new IntOrString(port)).endBackend().endPath().endHttp().endRule()
                .endSpec().build();
    }

    public static Ingress addPathToKubernetesIngressResource(Ingress ingress, Function function, int port) {
        ingress.getSpec().getRules().get(0).getHttp().getPaths().add(new HTTPIngressPathBuilder().withPath(function.getResourcePath())
                .withPathType("Prefix").withNewBackend().withServiceName(function.getName()).withServicePort(new IntOrString(port)).endBackend().build());
        return ingress;
    }

    //public static ConfigMap createKubernetesConfigMapResource(String serviceAccountName, String namespace) {}

    public static void main(String[] args) throws JsonProcessingException {
        Function function = new Function();
        function.setName("function-java");
        function.setImageName("lenneflow/function-java");
        function.setResourcePath("/function-java");

        Function function2 = new Function();
        function2.setName("function-ts");
        function2.setImageName("lenneflow/function-ts");
        function2.setResourcePath("/function-ts");

        Worker worker = new Worker();
        worker.setName("worker1");
        worker.setHostName("lenneflowworker");

        ObjectMapper mapper = new YAMLMapper();

        Ingress ingress = createKubernetesIngressResource(worker, function, 8080);
        String resource = mapper.writeValueAsString(ingress);
        System.out.println(resource);

        ingress = addPathToKubernetesIngressResource(ingress, function2, 8081);
        resource = mapper.writeValueAsString(ingress);
        System.out.println(resource);
    }

    public static void main_(String[] args) throws IOException, URISyntaxException {
        Yaml yaml = new Yaml();
        Map<String, Object> all = new HashMap<>();
        ObjectMapper mapper = new YAMLMapper();
        URL path = new URL("https://raw.githubusercontent.com/lenneflow/function-java/master/k8s");
        URL path2 = new URL("file:///C:/Users/IdrissaGANEMTORE/Documents/Custom%20Office%20Templates/");
        URLConnection connection = path.openConnection();
        //Map<String, Object> obj = yaml.load(connection.getInputStream());
        //String resource = mapper.writeValueAsString(obj);
        //System.out.println(resource);


    }
}
