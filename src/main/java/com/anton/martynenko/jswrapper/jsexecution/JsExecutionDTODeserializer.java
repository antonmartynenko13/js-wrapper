package com.anton.martynenko.jswrapper.jsexecution;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * JsExecution deserializer. Incoming serialized body can contain only script body.
 * @author Martynenko Anton
 * @since 1.2
 */

public class JsExecutionDTODeserializer extends JsonDeserializer<JsExecutionDTO> {

  @Override
  public JsExecutionDTO deserialize(@NotNull final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    return new JsExecutionDTO(node.get("scriptBody").asText());
  }
}
