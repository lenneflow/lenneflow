package de.lenneflow.workerservice.model;

import de.lenneflow.workerservice.enums.CloudProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class CloudCluster {

    @Id
    private String uid;

    @Indexed(unique = true)
    private String clusterName;

    private String description;

    private CloudProvider cloudProvider;

    private String region;

    private boolean create;

    private String cloudCredentialUid;

    private String roleArn;

    private String securityGroupId;

    private List<String> subnetIds = new ArrayList<>();
}
