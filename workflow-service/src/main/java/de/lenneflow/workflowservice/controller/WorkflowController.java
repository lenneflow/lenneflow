package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.JsonSchemaRepository;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.ObjectMapper;
import de.lenneflow.workflowservice.util.Validator;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
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

    @GetMapping("/{id}")
    public Workflow getWorkflow(@PathVariable String id) {
        return workflowRepository.findByUid(id);
    }

    @GetMapping("/workflow-name/{workflow-name}")
    public Workflow getWorkflowByName(@PathVariable("workflow-name") String name) {
        return workflowRepository.findByName(name);
    }

    @GetMapping("/list")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @PostMapping
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

    @PostMapping("/json-schema")
    public JsonSchema addJsonSchema(@RequestBody JsonSchema jsonSchema) {
        jsonSchema.setUid(UUID.randomUUID().toString());
        jsonSchema.setCreated(LocalDateTime.now());
        jsonSchema.setUpdated(LocalDateTime.now());
        validator.validateJsonSchema(jsonSchema);
        return jsonSchemaRepository.save(jsonSchema);
    }

    @GetMapping("/json-schema/list")
    public List<JsonSchema> getJsonSchemaList() {
        return jsonSchemaRepository.findAll();
    }

    @GetMapping("/json-schema/{uid}")
    public JsonSchema getJsonSchema(@PathVariable String uid) {
        return jsonSchemaRepository.findByUid(uid);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkflow(@PathVariable String id) {
        Workflow workflow = workflowRepository.findByUid(id);
        workflowStepRepository.deleteAll(workflow.getSteps());
        workflowRepository.delete(workflow);
    }

}
