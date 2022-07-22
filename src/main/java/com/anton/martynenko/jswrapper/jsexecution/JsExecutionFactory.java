package com.anton.martynenko.jswrapper.jsexecution;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Factory pattern realization for JsExecution entities.
 *
 * @author Martynenko Anton
 * @since 1.1
 */
@Component
public class JsExecutionFactory {

  /**
   * Factory method.
   * @param scriptBody code fragment string
   * @return new {@link JsExecution} object
   */
  public @NotNull JsExecution createNew(@NotNull final String scriptBody) {
    return new JsExecution(scriptBody);
  }

}
