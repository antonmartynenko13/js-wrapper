package com.anton.martynenko.jswrapper.jsexecution.constants;

/**
 * Response json body example strings. We need this values for informative openapi-documentation.
 *
 * @author Martynenko Anton
 * @since 1.1
 */

public final class JsonExamples {
  /**
   * Json example of {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object .
   */

  public static final String JS_EXECUTION_EXAMPLE = "{\n" +
      "    \"id\": 1,\n" +
      "    \"resultValue\": \"undefined\",\n" +
      "    \"status\": \"SUCCESSFUL\",\n" +
      "    \"scheduledTime\": \"2022-06-30T21:34:09.399+03:00[Europe/Minsk]\",\n" +
      "    \"executionTime\": \"2022-06-30T21:34:09.455+03:00[Europe/Minsk]\",\n" +
      "    \"links\": [\n" +
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
      "        },\n" +
      "        {\n" +
      "            \"rel\": \"exceptioninfo\",\n" +
      "            \"href\": \"http://localhost:8080/executions/1/exceptioninfo\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";

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

  public static final String NO_RESPONSE_BODY_EXAMPLE = "No response body";
  /**
   * Default constructor .
   */
  private JsonExamples() {
  }
}
