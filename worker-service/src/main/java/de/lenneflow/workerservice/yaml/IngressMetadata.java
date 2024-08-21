package de.lenneflow.workerservice.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngressMetadata {
    private String name;
    private Map<String, String> annotations;
}
