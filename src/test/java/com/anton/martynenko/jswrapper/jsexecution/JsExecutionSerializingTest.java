package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JsExecutionSerializingTest {

  private static final String JS_CONSOLE_OUTPUT = "Js console output";
  private static final String VALID_CODE_EXAMPLE = String.format("console.log('%s')", JS_CONSOLE_OUTPUT);

  @Autowired
  private JacksonTester<JsExecution> jacksonTester;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSerializeCorrectly() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Integer id = 1;
    JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);
  //  jsExecution.setId(id);
    Method setIdMethod = jsExecution.getClass().getDeclaredMethod("setId", Integer.class);
    setIdMethod.setAccessible(true);
    setIdMethod.invoke(jsExecution, id);

    JsonContent<JsExecution> result = jacksonTester.write(jsExecution);
    System.out.println(result.getJson());
    assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id);
    assertThat(result).extractingJsonPathStringValue("$.resultValue").isNull();
    assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(Status.CREATED.name());
    assertThat(result).extractingJsonPathStringValue("$.scheduledTime").isNotEmpty();
    assertThat(result).extractingJsonPathStringValue("$.executionTime").isNull();
    assertThat(result).extractingJsonPathStringValue("$.links[0].rel").isEqualTo("SCRIPTBODY");
    assertThat(result).extractingJsonPathStringValue("$.links[0].href").isEqualTo(String.format("/executions/%d/SCRIPTBODY", id));
    assertThat(result).extractingJsonPathStringValue("$.links[1].rel").isEqualTo("EXECUTIONLOG");
    assertThat(result).extractingJsonPathStringValue("$.links[1].href").isEqualTo(String.format("/executions/%d/EXECUTIONLOG", id));
    assertThat(result).extractingJsonPathStringValue("$.links[2].rel").isEqualTo("ERRORLOG");
    assertThat(result).extractingJsonPathStringValue("$.links[2].href").isEqualTo(String.format("/executions/%d/ERRORLOG", id));

  }
}
