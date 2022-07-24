package com.anton.martynenko.jswrapper.jsexecution;


import com.anton.martynenko.jswrapper.jsexecution.constants.JsonExamples;
import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeExecutedProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
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
   * {@link JsExecutionModelAssembler} bean.
   */
  private final JsExecutionModelAssembler jsExecutionModelAssembler;

  /**
   * Autowiring constructor.
   * @param jsExecutionService JsExecutionService service bean
   * @param jsExecutionModelAssembler JsExecutionModelAssembler service bean
   */
  @Autowired
  public JsExecutionController(final JsExecutionService jsExecutionService
                              , final JsExecutionModelAssembler jsExecutionModelAssembler) {
    this.jsExecutionService = jsExecutionService;
    this.jsExecutionModelAssembler = jsExecutionModelAssembler;
  }


  /**
   * Runs new JS code execution.
   *
   * @param newJsExecution new JsExecution to save and run
   * @return  {@link ResponseEntity} with containing json view of {@link JsExecution} with HATEOAS links
   * @throws JsExecutionCanNotBeExecutedProblem when JsExecution.scriptBody can't be executed
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
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "",
          description = "Execution can not be created",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_CAN_NOT_BE_EXECUTED_PROBLEM_EXAMPLE)))
  })
  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<?> createJsExecution(@RequestBody JsExecution newJsExecution) throws JsExecutionCanNotBeExecutedProblem {

    newJsExecution = jsExecutionService.saveAndExecute(newJsExecution);

    EntityModel<JsExecution> entityModel = jsExecutionModelAssembler.toModel(newJsExecution);

    return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
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
  public CollectionModel<EntityModel<JsExecution>>  listJsExecutions(@RequestParam(required = false) final Status status,
                                                                  @RequestParam(required = false) final SortBy sortBy) {

    List<EntityModel<JsExecution>> jsExecutions = jsExecutionService.getJsExecutions(status, sortBy).stream() //
        .map(jsExecutionModelAssembler::toModel) //
        .collect(Collectors.toList());

    return CollectionModel.of(jsExecutions, linkTo(methodOn(JsExecutionController.class)
        .listJsExecutions(null, null)).withSelfRel());
  }



  /**
   * Returns {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return  {@link EntityModel} containing json view of {@link JsExecution} with HATEOAS links
   *
   * @throws JsExecutionNotFoundProblem when no JsExecution with such id
   * @since 1.0
   */

  @Operation(summary = "Get JsExecution",
      description = "Get JsExecution by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "JsExecution found",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "JsExecution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE))),
  })
  @GetMapping(value = "/{executionId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public EntityModel<JsExecution> getJsExecution(@PathVariable final Integer executionId) throws JsExecutionNotFoundProblem{
    JsExecution jsExecution = jsExecutionService.getJsExecution(executionId);

    return jsExecutionModelAssembler.toModel(jsExecution);
  }


  /**
   * Returns script body plain text of {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @param property {@link  JsExecution}'s property string view
   * @return plain text or empty body
   * @throws JsExecutionNotFoundProblem when no JsExecution with such id
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
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE))),
  })
  @GetMapping(value = "/{executionId}/{property}", produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<String> getExecutionDetails(@PathVariable final Integer executionId, @PathVariable final Property property)
      throws JsExecutionNotFoundProblem{
    JsExecution jsExecution = jsExecutionService.getJsExecution(executionId);
    String detailsText = "";
    switch (property) {
      case SCRIPTBODY: detailsText = jsExecution.getScriptBody();
                        break;
      case EXECUTIONLOG: detailsText = jsExecution.collectExecutionLog();
                        break;
      case ERRORLOG: detailsText = jsExecution.collectErrorLog();
                        break;
      case EXCEPTIONINFO: detailsText = jsExecution.collectExceptionInfo();
                        break;
      default: //do nothing, we actually use enum to avoid unknown values
    }

    return ResponseEntity.ok(detailsText);
  }


  /**
   * Delete {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return empty response body
   * @throws JsExecutionNotFoundProblem when no JsExecution with such id
   *
   * @since 1.0
   */

  @Operation(summary = "Delete JsExecution",
      description = "Delete JsExecution by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NO_CONTENT + "", description = "Execution deleted"),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE)))
  })
  @DeleteMapping(value = "/{executionId}", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
  public ResponseEntity<String> deleteJsExecution(@PathVariable final Integer executionId) throws JsExecutionNotFoundProblem {
    jsExecutionService.deleteJsExecution(jsExecutionService.getJsExecution(executionId));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }



  /**
   * Cancel (or stop) execution by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return  {@link ResponseEntity} containing json view of {@link JsExecution} with HATEOAS links
   *
   * @since 1.0
   */

  @Operation(summary = "Cancel or stop JsExecution",
      description = "Cancel or stop JsExecution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Execution cancelled or stopped",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE , examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_METHOD + "", description = "JsExecution can't be cancelled",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_CAN_NOT_BE_CANCELLED_EXAMPLE)))
  })
  @DeleteMapping(value = "/{executionId}/cancel", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<?> cancelJsExecution(@PathVariable final Integer executionId) {
    JsExecution jsExecution = jsExecutionService.getJsExecution(executionId);

    if (!jsExecution.isCancelable()) {
      throw new JsExecutionCanNotBeCancelledProblem(String.format("JsExecution with status %s cant be cancelled.", jsExecution.getStatus()));
    }
    jsExecution = jsExecutionService.cancelJsExecution(jsExecution);
    return ResponseEntity.ok(jsExecutionModelAssembler.toModel(jsExecution));
  }
}
