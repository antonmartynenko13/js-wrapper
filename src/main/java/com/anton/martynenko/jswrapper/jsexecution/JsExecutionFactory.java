package com.anton.martynenko.jswrapper.jsexecution;

import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

/**
 * Simple factory component. Produces new {@link JsExecution} instances.
 * @author Martynenko Anton
 * @since 1.2
 */

@Component
public class JsExecutionFactory {

  /**
   * Simple factory method. Produces new {@link JsExecution} instances.
   * @param scriptBody javascript code fragment
   * @return new {@link JsExecution} instance
   */

  @NotNull
  JsExecution createNew(@NotNull final String scriptBody) {
    return new JsExecution(scriptBody);
  }
}
