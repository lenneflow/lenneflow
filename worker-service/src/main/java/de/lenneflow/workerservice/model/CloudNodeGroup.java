package de.lenneflow.workerservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
@CompoundIndex(def = "{'clusterUid': 1, 'groupName': 1}", unique = true)
public class CloudNodeGroup {

    @Id
    private String uid;

    private String clusterUid;

    private String groupName;

    private String description;

    private boolean create;

    private String ami;

    private String instanceType;

    private int minNodeCount;

    private int desiredNodeCount;

    private int maxNodeCount;

    private String roleArn;

    private List<String> subnetIds = new ArrayList<>();

    private LocalDateTime created;

    private LocalDateTime updated;
}
