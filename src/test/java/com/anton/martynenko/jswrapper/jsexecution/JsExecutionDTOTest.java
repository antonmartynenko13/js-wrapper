package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JsExecutionDTOTest {

  private static final String JS_CONSOLE_OUTPUT = "Js console output";
  private static final String VALID_CODE_EXAMPLE = String.format("console.log('%s')", JS_CONSOLE_OUTPUT);

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private JacksonTester<JsExecutionDTO> jacksonTester;

  @Test
  void ShouldSerializeCorrectly() throws IOException {
    int id = 1;

    ZonedDateTime scheduled = ZonedDateTime.now();
    String scheduledString = scheduled.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    ZonedDateTime executed = ZonedDateTime.now();
    String executedString = executed.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);

    JsExecutionDTO jsExecutionDTO = new JsExecutionDTO(id, Status.SUCCESSFUL, VALID_CODE_EXAMPLE, scheduled, executed,
        "Execution log", "Error log", false, "Exception info");

    JsonContent<JsExecutionDTO> result = jacksonTester.write(jsExecutionDTO);
    System.out.println(result.getJson());
    assertThat(result).extractingJsonPathNumberValue("$.['id']").isEqualTo(id);
    assertThat(result).extractingJsonPathStringValue("$.['status']").isEqualTo(Status.SUCCESSFUL.name());
    assertThat(result).extractingJsonPathStringValue("$.['Executed at']").isEqualTo(executedString);
    assertThat(result).extractingJsonPathStringValue("$.['Scheduled at']").isEqualTo(scheduledString);
  }
}
