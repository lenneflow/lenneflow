package de.lenneflow.workerservice.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlEditor {

    public static void main__(String[] args) throws JsonProcessingException {
        IngressYamlObject object = new IngressYamlObject();
        Map<String, String> annotations = new HashMap<String, String>();
        annotations.put("kubernetes.io/ingress.class", "nginx");
        annotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
        object.setMetadata(new IngressMetadata("worker-ingress", annotations));
        Path path = new Path("/api/functionjava","Prefix",new Backend(new Service("funtion-java",new Port("8080"))));
        List<Path> paths = new ArrayList<Path>();
        paths.add(path);
        object.setSpec(new IngressSpecs("nginx", new Rules("lenneflowworker", new Http(paths))));
        ObjectMapper mapper = new YAMLMapper();
       System.out.println(mapper.writeValueAsString(object));

    }

    public static void main(String[] args) throws IOException {
        Yaml yaml = new Yaml();
        ObjectMapper mapper = new YAMLMapper();
        URL path = new URL("https://raw.githubusercontent.com/lenneflow/function-java/master/k8s/deployment.yaml");
        URLConnection connection = path.openConnection();
        Map<String, Object> obj = yaml.load(connection.getInputStream());
        String resource = mapper.writeValueAsString(obj);
        System.out.println(resource);
    }
}
