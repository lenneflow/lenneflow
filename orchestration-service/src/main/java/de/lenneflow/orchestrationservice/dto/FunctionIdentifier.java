package de.lenneflow.orchestrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FunctionIdentifier {

    private String executionId;

    private String stepInstanceId;

    private String workflowInstanceId;
}
