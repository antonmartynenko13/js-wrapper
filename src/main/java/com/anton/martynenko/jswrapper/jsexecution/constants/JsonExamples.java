package com.anton.martynenko.jswrapper.jsexecution.constants;

/**
 * Response json body example constants. We need this values for informative openapi-documentation.
 *
 * @author Martynenko Anton
 * @since 1.1
 */

public final class JsonExamples {

  /**
   * Hidden empty constructor to forbid instance's creation .
   */
  private JsonExamples() {
  }

  /**
   * Json example of submitted {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object .
   */

  public static final String JS_EXECUTION_SUBMITTED_EXAMPLE = "{\n" +
      "    \"id\": 0,\n" +
      "    \"status\": \"SUBMITTED\",\n" +
      "    \"Scheduled at\": \"2022-08-15T00:12:53.468+03:00[Europe/Minsk]\",\n" +
      "    \"Executed at\": null,\n" +
      "    \"links\": [\n" +
      "        {\n" +
      "            \"rel\": \"self\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"jsExecutions\",\n" +
      "            \"href\": \"http://localhost:8080/executions{?status,sortBy}\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"delete\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"cancel\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0/cancel\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"scriptbody\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0/scriptbody\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";

  /**
   * Json example of success {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object .
   */

  public static final String JS_EXECUTION_SUCCESS_EXAMPLE = "{\n" +
      "    \"id\": 0,\n" +
      "    \"status\": \"SUCCESSFUL\",\n" +
      "    \"Scheduled at\": \"2022-08-15T00:12:53.468+03:00[Europe/Minsk]\",\n" +
      "    \"Executed at\": \"2022-08-15T00:12:54.454+03:00[Europe/Minsk]\",\n" +
      "    \"links\": [\n" +
      "        {\n" +
      "            \"rel\": \"self\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"jsExecutions\",\n" +
      "            \"href\": \"http://localhost:8080/executions{?status,sortBy}\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"delete\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"scriptbody\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0/scriptbody\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"executionlog\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0/executionlog\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"errorlog\",\n" +
      "            \"href\": \"http://localhost:8080/executions/0/errorlog\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";

  /**
   * Json example of cancelled {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object .
   */

  public static final String JS_EXECUTION_CANCELLED_EXAMPLE = "{\n" +
      "    \"id\": 1,\n" +
      "    \"status\": \"CANCELLED\",\n" +
      "    \"Scheduled at\": \"2022-08-15T00:16:47.446+03:00[Europe/Minsk]\",\n" +
      "    \"Executed at\": null,\n" +
      "    \"links\": [\n" +
      "        {\n" +
      "            \"rel\": \"self\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"jsExecutions\",\n" +
      "            \"href\": \"http://localhost:8080/executions{?status,sortBy}\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"delete\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"scriptbody\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1/scriptbody\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"executionlog\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1/executionlog\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"errorlog\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1/errorlog\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object used as post query's body.
   */

  public static final String CREATE_JS_EXECUTION_REQUEST_BODY_EXAMPLE = "{\"scriptBody\": \"console.log('I am js snippet!');\"}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} objects array .
   */
  public static final String JS_EXECUTION_ARRAY_EXAMPLE = "[\n" +
      JS_EXECUTION_SUCCESS_EXAMPLE + ", \n" +
      JS_EXECUTION_CANCELLED_EXAMPLE +
      "]";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem} object .
   */
  public static final String JS_EXECUTION_NOT_FOUND_EXAMPLE = "{\n" +
      "    \"title\": \"Not Found\",\n" +
      "    \"status\": 404,\n" +
      "    \"detail\": \"JsExecution id 10 not found\"\n" +
      "}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem} object .
   */
  public static final String JS_EXECUTION_CAN_NOT_BE_CANCELLED_EXAMPLE = "{\n" +
      "    \"title\": \"Method Not Allowed\",\n" +
      "    \"status\": 405,\n" +
      "    \"detail\": \"JsExecution id 1 and status CANCELLED can't be canceled\"\n" +
      "}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.problem.NoSuchPropertyProblem} object .
   */
  public static final String NO_SUCH_JS_EXECUTION_PROPERTY_EXAMPLE = "{\n" +
      "    \"title\": \"Bad Request\",\n" +
      "    \"status\": 400,\n" +
      "    \"detail\": \"JsExecution contains no 'wrongproperty' details property\"\n" +
      "}";

  /**
   * Plain text response example .
   */
  public static final String PLAIN_TEXT_EXAMPLE = "This is details report in plain text format. \n" +
      "It could be very varied.";

}
