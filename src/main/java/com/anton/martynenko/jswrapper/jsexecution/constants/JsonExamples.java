package com.anton.martynenko.jswrapper.jsexecution.constants;

/**
 * Response json body example strings. We need this values for informative openapi-documentation.
 *
 * @author Martynenko Anton
 * @since 1.1
 */

public abstract class JsonExamples {
  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object .
   */

  public static final String JS_EXECUTION_EXAMPLE = "{\n" +
      "    \"id\": 2,\n" +
      "    \"resultValue\": null,\n" +
      "    \"status\": \"RUNNING\",\n" +
      "    \"scheduledTime\": \"2022-07-24T13:28:11.2030535+03:00[Europe/Minsk]\",\n" +
      "    \"executionTime\": null,\n" +
      "    \"links\": [\n" +
      "        {\n" +
      "            \"rel\": \"self\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"jsExecutions\",\n" +
      "            \"href\": \"http://localhost:8080/executions{?status,sortBy}\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"SCRIPTBODY\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2/SCRIPTBODY\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"EXECUTIONLOG\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2/EXECUTIONLOG\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"ERRORLOG\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2/ERRORLOG\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"EXCEPTIONINFO\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2/EXCEPTIONINFO\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"cancel\",\n" +
      "            \"href\": \"http://localhost:8080/executions/2/cancel\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";

  public static final String CREATE_JS_EXECUTION_REQUEST_BODY_EXAMPLE = "{\"scriptBody\": \"console.log('I am js snippet!');\"}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} objects array .
   */
  public static final String JS_EXECUTION_ARRAY_EXAMPLE = "[\n" +
        JS_EXECUTION_EXAMPLE +
      "]";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeExecutedProblem} object .
   */

  public static final String JSEXECUTION_CAN_NOT_BE_EXECUTED_PROBLEM_EXAMPLE = "{\n" +
      "    \"title\": \"Code parsing ends with problem\",\n" +
      "    \"status\": 400,\n" +
      "    \"detail\": \"SyntaxError: Unnamed:1:5 Expected ; but found ole\\r\\ncons ole.log('Slow function starts');\\r\\n     ^\\n\\nSourceSection(source=Unnamed [1:6 - 1:8], index=5, length=3, characters=ole)\"\n" +
      "}";

  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem} object .
   */
  public static final String JSEXECUTION_NOT_FOUND_EXAMPLE = "{\n" +
      "    \"title\": \"Not Found\",\n" +
      "    \"status\": 404,\n" +
      "    \"detail\": \"JsExecution id 10 not found\"\n" +
      "}";

  public static final String PLAIN_TEXT_EXAMPLE = "This is details report in plain text format. \n" +
      "It could be very varied.";

  public static final String JSEXECUTION_CAN_NOT_BE_CANCELLED_EXAMPLE = "{\n" +
      "    \"title\": \"Method not allowed\",\n" +
      "    \"status\": 405,\n" +
      "    \"detail\": \"You can't cancel an execution that is in the SUCCESSFUL status\"\n" +
      "}";


}
