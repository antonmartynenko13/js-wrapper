package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JsExecutionSerializingTest {

  private static final String JS_CONSOLE_OUTPUT = "Js console output";
  private static final String VALID_CODE_EXAMPLE = String.format("console.log('%s')", JS_CONSOLE_OUTPUT);

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private JacksonTester<JsExecution> jacksonTester;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSerializeCorrectly() throws IOException, IllegalAccessException, NoSuchFieldException {
    Integer id = 1;
    JsExecution jsExecution = new JsExecution(VALID_CODE_EXAMPLE);
    FieldUtils.writeField(jsExecution, "id", id, true);

    JsonContent<JsExecution> result = jacksonTester.write(jsExecution);
    System.out.println(result.getJson());
    assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id);
    assertThat(result).extractingJsonPathStringValue("$.resultValue").isNull();
    assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(Status.CREATED.name());
    assertThat(result).extractingJsonPathStringValue("$.scheduledTime").isNotEmpty();
    assertThat(result).extractingJsonPathStringValue("$.executionTime").isNull();

  }
}
