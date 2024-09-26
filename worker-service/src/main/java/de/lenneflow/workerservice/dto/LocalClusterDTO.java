package de.lenneflow.workerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LocalClusterDTO {

    private String clusterName;

    private String description;

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private String apiServerEndpoint;

    private String apiAuthToken;

    private String hostName;

}
