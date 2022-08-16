package com.anton.martynenko.jswrapper.jsexecution;


import com.anton.martynenko.jswrapper.jsexecution.constants.JsonExamples;
import com.anton.martynenko.jswrapper.jsexecution.constants.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.NoSuchPropertyProblem;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


/**
 * Main Spring MVC REST-controller.
 *
 * @author Martynenko Anton
 * @since 1.0
 */

@OpenAPIDefinition(info =
    @Info(
        title = "JS executions",
        version = "1.1",
        description = "JS executions API"
    )
)
@Tag(name = "JS executions API")
@RestController
@RequestMapping("/executions")
public class JsExecutionController {

  /**
   * {@link JsExecutionService}JsExecutionService bean.
   */
  private final JsExecutionService jsExecutionService;

  /**
   * {@link JsExecutionDTOModelAssembler} bean.
   */
  private final JsExecutionDTOModelAssembler jsExecutionDTOModelAssembler;

  /**
   * Autowiring constructor.
   * @param jsExecutionService JsExecutionService service bean
   * @param jsExecutionDTOModelAssembler JsExecutionModelAssembler service bean
   */
  @Autowired
  public JsExecutionController(final JsExecutionService jsExecutionService,
                               final JsExecutionDTOModelAssembler jsExecutionDTOModelAssembler) {
    this.jsExecutionService = jsExecutionService;
    this.jsExecutionDTOModelAssembler = jsExecutionDTOModelAssembler;
  }


  /**
   * Runs new JS code execution.
   *
   * @param newJsExecutionDTO new JsExecution to save and run
   * @return  {@link ResponseEntity} with containing json view of {@link JsExecution} with HATEOAS links
   *
   * @since 1.0
   */


  @Operation(summary = "Create new JsExecution",
      description = "Create new JsExecution")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                  examples = @ExampleObject(value = JsonExamples.CREATE_JS_EXECUTION_REQUEST_BODY_EXAMPLE)))
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_CREATED + "",
          description = "Code execution request created",
          headers = @Header(name = "Location", description = "Location of created execution"),
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_SUBMITTED_EXAMPLE)))
  })
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityModel<JsExecutionDTO>> createNew(@RequestBody final JsExecutionDTO newJsExecutionDTO) {

    JsExecutionDTO jsExecutionDTO = jsExecutionService.createAndRun(newJsExecutionDTO);

    EntityModel<JsExecutionDTO> entityModel = jsExecutionDTOModelAssembler.toModel(jsExecutionDTO);

    return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
  }

  /**
   * Returns {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id.
   * @return  {@link EntityModel} containing json view of {@link JsExecution} with HATEOAS links
   *
   * @since 1.0
   */

  @Operation(summary = "Get JsExecution",
      description = "Get JsExecution by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "JsExecution found",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_SUCCESS_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "JsExecution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_NOT_FOUND_EXAMPLE))),
  })
  @GetMapping(value = "/{executionId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public EntityModel<JsExecutionDTO> getOne(@PathVariable final Integer executionId) {
    return jsExecutionDTOModelAssembler.toModel(jsExecutionService.getOne(executionId));
  }

  /**
   * Returns {@link  JsExecution} collection.
   *
   * @param status optional {@link  com.anton.martynenko.jswrapper.jsexecution.enums.Status} filtration criteria
   * @param sortBy optional {@link  com.anton.martynenko.jswrapper.jsexecution.enums.SortBy} sorting criteria
   * @return  {@link CollectionModel}  of {@link EntityModel} ({@link JsExecution} json view with HATEOAS links)
   *
   * @since 1.0
   */

  @Operation(summary = "List JsExecutions",
      description = "Retrieve JsExecutions list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Request is successful", content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_ARRAY_EXAMPLE))
      }),
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CollectionModel<EntityModel<JsExecutionDTO>>  listAll(@RequestParam(required = false) final Status status,
                                                                     @RequestParam(required = false) final SortBy sortBy) {

    List<EntityModel<JsExecutionDTO>> entityModels = jsExecutionService.findAll(status, sortBy).stream() //
        .map(jsExecutionDTOModelAssembler::toModel) //
        .collect(Collectors.toList());

    return CollectionModel.of(entityModels, linkTo(methodOn(JsExecutionController.class)
        .listAll(null, null)).withSelfRel());
  }

  /**
   * Cancel execution by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return  {@link ResponseEntity} containing json view of {@link JsExecution} with HATEOAS links
   * @since 1.0
   */

  @Operation(summary = "Cancel or stop JsExecution",
      description = "Cancel or stop JsExecution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Execution cancelled or stopped",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_CANCELLED_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_NOT_FOUND_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_METHOD + "", description = "JsExecution can't be cancelled",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_CAN_NOT_BE_CANCELLED_EXAMPLE)))
  })
  @DeleteMapping(value = "/{executionId}/cancel", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<EntityModel<JsExecutionDTO>> cancelJsExecution(@PathVariable final Integer executionId) {
    JsExecutionDTO jsExecutionDTO = jsExecutionService.cancelExecution(executionId);
    return ResponseEntity.ok(jsExecutionDTOModelAssembler.toModel(jsExecutionDTO));
  }


  /**
   * Delete {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return empty response body
   *
   * @since 1.0
   */

  @Operation(summary = "Delete JsExecution",
      description = "Delete JsExecution by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NO_CONTENT + "", description = "Execution deleted",
      content = @Content(mediaType = MediaType.ALL_VALUE)),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_NOT_FOUND_EXAMPLE)))
  })
  @DeleteMapping(value = "/{executionId}", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
  public ResponseEntity<String> deleteJsExecution(@PathVariable final Integer executionId) {
    jsExecutionService.deleteExecution(executionId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }


  /**
   * Returns script body plain text of {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @param property {@link  JsExecution}'s property string view
   * @return plain text or empty body
   *
   * @since 1.1
   */

  @Operation(summary = "Get execution details as plain text",
      description = "Get execution details by JS execution id and property name as plain text")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Detailed property returned",
          content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
              examples = @ExampleObject(value = JsonExamples.PLAIN_TEXT_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_NOT_FOUND_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "No such details property",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.NO_SUCH_JS_EXECUTION_PROPERTY_EXAMPLE))),
  })
  @GetMapping(value = "/{executionId}/{property}", produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<String> getDetails(@PathVariable final Integer executionId, @PathVariable final String property) {
    JsExecutionDTO jsExecutionDTO = jsExecutionService.getOne(executionId);
    String detailsText = "";
    switch (property) {
      case Property.SCRIPT_BODY : detailsText = jsExecutionDTO.getScriptBody();
        break;
      case Property.EXECUTION_LOG: detailsText = jsExecutionDTO.getExecutionLog();
        break;
      case Property.ERROR_LOG: detailsText = jsExecutionDTO.getErrorLog();
        break;
      case Property.EXCEPTION_INFO: detailsText = jsExecutionDTO.getExceptionInfo();
        break;
      default:
        throw new NoSuchPropertyProblem(property);
    }

    return ResponseEntity.ok(detailsText);
  }

}
