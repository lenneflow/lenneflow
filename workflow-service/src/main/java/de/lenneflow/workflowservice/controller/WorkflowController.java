package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.JsonSchemaDTO;
import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.JsonSchemaRepository;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.ObjectMapper;
import de.lenneflow.workflowservice.util.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@Tag(name = "Workflows API")
public class WorkflowController {

    final WorkflowRepository workflowRepository;
    final WorkflowStepRepository workflowStepRepository;
    final JsonSchemaRepository jsonSchemaRepository;
    private final Validator validator;

    public WorkflowController(WorkflowRepository workflowRepository, WorkflowStepRepository workflowStepRepository, JsonSchemaRepository jsonSchemaRepository, Validator validator) {
        this.workflowRepository = workflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.jsonSchemaRepository = jsonSchemaRepository;
        this.validator = validator;
    }

    @Operation(summary = "Get Workflow by UID")
    @GetMapping("/{uid}")
    public Workflow getWorkflow(@PathVariable String uid) {
        return workflowRepository.findByUid(uid);
    }

    @Operation(summary = "Get Workflow by Name")
    @GetMapping("/name/{workflow-name}")
    public Workflow getWorkflowByName(@PathVariable("workflow-name") String name) {
        return workflowRepository.findByName(name);
    }

    @Operation(summary = "Get all Workflows")
    @GetMapping("/list")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }


    @Operation(summary = "Create a new Workflow")
    @PostMapping("/create")
    public Workflow addNewWorkflow(@RequestBody WorkflowDTO workflowDTO) {
        validator.validate(workflowDTO);
        Workflow workflow = ObjectMapper.mapToWorkflow(workflowDTO);
        JsonSchema inputSchema = jsonSchemaRepository.findByUid(workflowDTO.getInputDataSchemaUid());
        workflow.setInputDataSchema(inputSchema);
        JsonSchema outputSchema = jsonSchemaRepository.findByUid(workflowDTO.getOutputDataSchemaUid());
        workflow.setOutputDataSchema(outputSchema);
        workflow.setUid(UUID.randomUUID().toString());
        workflow.setCreated(LocalDateTime.now());
        workflow.setUpdated(LocalDateTime.now());
        validator.validate(workflow);
        return workflowRepository.save(workflow);
    }

    @Operation(summary = "Create a new JSON Schema")
    @PostMapping("/json-schema/create")
    public JsonSchema addJsonSchema(@RequestBody JsonSchemaDTO jsonSchemaDTO) {
        JsonSchema jsonSchema = ObjectMapper.mapToJsonSchema(jsonSchemaDTO);
        jsonSchema.setUid(UUID.randomUUID().toString());
        jsonSchema.setCreated(LocalDateTime.now());
        jsonSchema.setUpdated(LocalDateTime.now());
        validator.validateJsonSchema(jsonSchema);
        return jsonSchemaRepository.save(jsonSchema);
    }

    @Operation(summary = "Get all JSON Schema")
    @GetMapping("/json-schema/list")
    public List<JsonSchema> getJsonSchemaList() {
        return jsonSchemaRepository.findAll();
    }

    @Operation(summary = "Get JSON Schema by UID")
    @GetMapping("/json-schema/{uid}")
    public JsonSchema getJsonSchema(@PathVariable String uid) {
        return jsonSchemaRepository.findByUid(uid);
    }

    @Operation(summary = "Delete JSON Schema by UID")
    @DeleteMapping("/json-schema/{uid}")
    public void deleteJsonSchema(@PathVariable String uid) {
        jsonSchemaRepository.delete(jsonSchemaRepository.findByUid(uid));
    }

    @Operation(summary = "Delete the Workflow and all Steps")
    @DeleteMapping("/{uid}")
    public void deleteWorkflow(@PathVariable String uid) {
        Workflow workflow = workflowRepository.findByUid(uid);
        workflowStepRepository.deleteAll(workflow.getSteps());
        workflowRepository.delete(workflow);
    }

}
