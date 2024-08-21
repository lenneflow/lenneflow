package de.lenneflow.workerservice.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngressYamlObject {
    private String apiVersion = "networking.k8s.io/v1";
    private String kind = "Ingress";
    private IngressMetadata metadata;
    private IngressSpecs spec;
}
