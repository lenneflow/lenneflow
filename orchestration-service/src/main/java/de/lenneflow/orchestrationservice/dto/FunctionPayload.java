package de.lenneflow.orchestrationservice.dto;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FunctionPayload {

    private RunStatus runStatus;

    private String callBackLink;

    private String failureReason;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
