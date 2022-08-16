package com.anton.martynenko.jswrapper.jsexecution;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class JsExecutionFactory {

  @NotNull
  JsExecution createNew(@NotNull final String scriptBody) {
    return new JsExecution(scriptBody);
  }
}
