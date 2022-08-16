package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.constants.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.hateoas.EntityModel;

import java.io.IOException;
import java.time.ZonedDateTime;

import static java.lang.String.format;


@JsonTest
class JsExecutionModelAssemblerTest {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private JacksonTester<EntityModel<JsExecutionDTO>> jacksonTester;

  private JsExecutionDTOModelAssembler jsExecutionModelAssembler = new JsExecutionDTOModelAssembler();

  @Test
  void toModel() throws IOException {

    int id = 0;

    JsExecutionDTO jsExecutionDTO = new JsExecutionDTO(id,
        Status.CREATED,
        "console.log('Some code');",
        ZonedDateTime.now(),
        null,
        "executionLog",
        "errorLog",
        true,
        "Exception info");


    EntityModel<JsExecutionDTO> jsExecutionDTOEntityModel = jsExecutionModelAssembler.toModel(jsExecutionDTO);

    JsonContent<EntityModel<JsExecutionDTO>> result = jacksonTester.write(jsExecutionDTOEntityModel);
    System.out.println(result.getJson());
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[0].rel")
        .isEqualTo("self");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[0].href")
        .isEqualTo(format("/executions/%d", id));
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[1].rel")
        .isEqualTo("jsExecutions");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[1].href")
        .isEqualTo("/executions{?status,sortBy}");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[2].rel")
        .isEqualTo("delete");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[2].href")
        .isEqualTo(format("/executions/%d", id));
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[3].rel")
        .isEqualTo("cancel");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[3].href")
        .isEqualTo(format("/executions/%d/cancel", id));

    Assertions.assertThat(result).extractingJsonPathStringValue(("$.links[4].rel"))
        .isEqualTo(Property.SCRIPT_BODY);
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[4].href")
        .isEqualTo(format("/executions/%d/%s", id, Property.SCRIPT_BODY));

    Assertions.assertThat(result).extractingJsonPathStringValue(("$.links[5].rel"))
        .isEqualTo(Property.EXCEPTION_INFO);
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[5].href")
        .isEqualTo(format("/executions/%d/%s", id, Property.EXCEPTION_INFO));

    Assertions.assertThat(result).extractingJsonPathStringValue(("$.links[6].rel"))
        .isEqualTo(Property.EXECUTION_LOG);
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[6].href")
        .isEqualTo(format("/executions/%d/%s", id, Property.EXECUTION_LOG));

    Assertions.assertThat(result).extractingJsonPathStringValue(("$.links[7].rel"))
        .isEqualTo(Property.ERROR_LOG);
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[7].href")
        .isEqualTo(format("/executions/%d/%s", id, Property.ERROR_LOG));

  }
}