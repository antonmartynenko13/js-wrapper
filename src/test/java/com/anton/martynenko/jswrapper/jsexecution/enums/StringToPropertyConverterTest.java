package com.anton.martynenko.jswrapper.jsexecution.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StringToPropertyConverterTest {

  private final StringToPropertyConverter stringToPropertyConverter = new StringToPropertyConverter();

  @Test
  void shouldCorrectlyConvert() {
    for (Property property: Property.values()){
      assertThat(stringToPropertyConverter.convert(property.name().toLowerCase())).isEqualTo(property);
    }
  }

  @Test
  void shouldThrowIllegalArgumentException(){
    assertThrows(IllegalArgumentException.class, () -> stringToPropertyConverter.convert("something invalid"));
  }
}