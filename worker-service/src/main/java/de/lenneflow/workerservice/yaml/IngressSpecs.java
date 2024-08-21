package de.lenneflow.workerservice.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngressSpecs {
    private String ingressClassName;
    private Rules rules;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Port {
    private String number;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Service{
    private String name;
    private Port port;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Backend{
    private Service service;
}


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Path{
    private String path;
    private String pathType;
    private Backend backend;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Http{
    private List<Path> paths;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Rules{
    private String host;
    private Http http;
}
