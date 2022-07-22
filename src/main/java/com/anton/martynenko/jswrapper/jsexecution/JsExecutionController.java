package com.anton.martynenko.jswrapper.jsexecution;


import com.anton.martynenko.jswrapper.jsexecution.constants.JsonExamples;
import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.util.Collection;


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
   * Pattern for /executions requests path.
   */
  private static final String JS_EXECUTION_PATH_PATTERN = "%s/executions/%s";

  /**
   * JsExecutionService bean.
   */
  private final JsExecutionService jsExecutionService;

  /**
   * Autowiring constructor.
   * @param jsExecutionService JsExecutionService service bean
   */
  @Autowired
  public JsExecutionController(final JsExecutionService jsExecutionService) {
    this.jsExecutionService = jsExecutionService;
  }


  /**
   * Runs new JS code execution.
   *
   * @param scriptBody code fragment
   * @param request HttpServletRequest object
   * @return execution object
   * @since 1.0
   */


  @Operation(summary = "Create and run script execution",
      description = "Create and run javascript function")
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
  public ResponseEntity<JsExecution> createJsExecution(@RequestParam final String scriptBody, final HttpServletRequest request) {

    JsExecution jsExecution = jsExecutionService.createJsExecution(scriptBody);

    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", String.format(JS_EXECUTION_PATH_PATTERN, request.getContextPath(), jsExecution.getId()))
        .body(jsExecution);
  }



  /**
   * Returns {@link  JsExecution} collection.
   *
   * @param status optional {@link  com.anton.martynenko.jswrapper.jsexecution.enums.Status} filtration criteria
   * @param sortBy optional {@link  com.anton.martynenko.jswrapper.jsexecution.enums.SortBy} sorting criteria
   * @return collection of {@link  JsExecution} objects or empty collection
   *
   * @since 1.0
   */

  @Operation(summary = "List JS executions",
      description = "Retrieve JS executions list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Request is successful", content = {
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_ARRAY_EXAMPLE))
      }),
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Collection<JsExecution>> listJsExecutions(@RequestParam(required = false) final Status status,
                                                                  @RequestParam(required = false) final SortBy sortBy) {
    Collection<JsExecution> jsExecutions = jsExecutionService.getJsExecutions(status, sortBy);

    return ResponseEntity.status(HttpStatus.OK).body(jsExecutions);
  }



  /**
   * Returns {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return  {@link  JsExecution} object or empty body
   * @since 1.0
   */

  @Operation(summary = "Get execution",
      description = "Get execution object by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "Execution found",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE))),
  })
  @GetMapping(value = "/{executionId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<JsExecution> getJsExecution(@PathVariable final Integer executionId) {
    JsExecution jsExecution = jsExecutionService.getJsExecution(executionId);

    return ResponseEntity.status(HttpStatus.OK).body(jsExecution);
  }


  /**
   * Returns script body plain text of {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @param property {@link  JsExecution}'s property string view
   * @return  {@link  JsExecution} script plain text or empty body
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
  public ResponseEntity<String> getExecutionDetails(@PathVariable final Integer executionId, @PathVariable final Property property) {
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

    return ResponseEntity.status(HttpStatus.OK).body(detailsText);
  }


  /**
   * Delete {@link  JsExecution} by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return empty response body
   *
   * @since 1.0
   */

  @Operation(summary = "Delete execution",
      description = "Delete execution by id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NO_CONTENT + "", description = "Execution deleted"),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE)))
  })
  @DeleteMapping(value = "/{executionId}", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
  public ResponseEntity<String> deleteJsExecution(@PathVariable final Integer executionId) {
    jsExecutionService.deleteJsExecution(jsExecutionService.getJsExecution(executionId));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }



  /**
   * Stops execution by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return empty response body
   *
   * @since 1.0
   */

  @Operation(summary = "Stop execution",
      description = "Create JS execution's stop request")
  @ApiResponses(value = {
      @ApiResponse(responseCode = HttpURLConnection.HTTP_ACCEPTED + "", description = "Execution stop request accepted",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE , examples = @ExampleObject(value = JsonExamples.JS_EXECUTION_EXAMPLE))),
      @ApiResponse(responseCode = HttpURLConnection.HTTP_NOT_FOUND + "", description = "Execution not found",
          content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
              examples = @ExampleObject(value = JsonExamples.JSEXECUTION_NOT_FOUND_EXAMPLE)))
  })
  @PostMapping(value = "/{executionId}/stoprequest", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
  public ResponseEntity<JsExecution> createJsExecutionStopRequest(@PathVariable final Integer executionId) {
    JsExecution jsExecution = jsExecutionService.getJsExecution(executionId);
    jsExecution = jsExecutionService.stopJsExecution(jsExecution);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsExecution);
  }
}
