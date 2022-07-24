package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.hateoas.EntityModel;

import java.io.IOException;

import static java.lang.String.format;


@JsonTest
class JsExecutionModelAssemblerTest {

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private JacksonTester<EntityModel<JsExecution>> jacksonTester;

  @Autowired
  private ObjectMapper objectMapper;

  private JsExecutionModelAssembler jsExecutionModelAssembler = new JsExecutionModelAssembler();

  @Test
  void toModel() throws IOException, NoSuchFieldException, IllegalAccessException {
    int id = 1;
    JsExecution jsExecution = new JsExecution("console.log('Some code');");
    FieldUtils.writeField(jsExecution, "id", id, true);

    EntityModel<JsExecution> jsExecutionEntityModel = jsExecutionModelAssembler.toModel(jsExecution);

    JsonContent<EntityModel<JsExecution>> result = jacksonTester.write(jsExecutionEntityModel);
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
        .isEqualTo("cancel");
    Assertions.assertThat(result).extractingJsonPathStringValue("$.links[2].href")
        .isEqualTo(format("/executions/%d/cancel", id));

    int index = 3;
    for (Property property: Property.values()){

      Assertions.assertThat(result).extractingJsonPathStringValue(format("$.links[%d].rel", index))
          .isEqualTo(property.name());
      Assertions.assertThat(result).extractingJsonPathStringValue(format("$.links[%d].href", index))
          .isEqualTo(format("/executions/%d/%s", id, property.name()));
      index++;
    }
  }
}