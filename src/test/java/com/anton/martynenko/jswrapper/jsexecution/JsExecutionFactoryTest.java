package com.anton.martynenko.jswrapper.jsexecution;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsExecutionFactoryTest {

  @Test
  void createNew() {
    JsExecutionFactory jsExecutionFactory = new JsExecutionFactory();
    assertThat(jsExecutionFactory.createNew("Some code")).isNotNull();
  }
}