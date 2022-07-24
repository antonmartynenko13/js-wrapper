package com.anton.martynenko.jswrapper.graalvm;

import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeExecutedProblem;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for interacting with Graal VM's API.
 *
 * @author Martynenko Anton
 * @since 1.1
 * @see <a href="https://www.graalvm.org/">GraalVM project</a>
 */
@Component
public class GraalVmHelper {

  /**
   * Local {@link org.slf4j.Logger} bean.
   */
  private final Logger logger;

  /**
   * Constructor autowiring {@link org.slf4j.Logger} bean.
   * @param logger {@link org.slf4j.Logger} bean
   */
  @Autowired
  public GraalVmHelper(final Logger logger) {
    this.logger = logger;
  }

  /**
   * Validate code fragment and checks possibility of execution.
   *
   * <p>If code is malformed, or cannot be executed {@link JsExecutionCanNotBeExecutedProblem} will be thrown</p>
   *
   * @param scriptBody code fragment
   * @param language {@link Language} view of code language
   * @throws JsExecutionCanNotBeExecutedProblem when JsExecution.scriptBody can't be executed
   */

  public void validate(final String scriptBody, final Language language) throws JsExecutionCanNotBeExecutedProblem{

    logger.debug("Validation of JS code fragment:\n{}", scriptBody);

    try (Context context = Context.newBuilder().build()) {
      Source source = Source.create(language.name().toLowerCase(), scriptBody);
      try {
        Value script = context.parse(source);
        if (!script.canExecute()) {

          logger.warn("Code fragment is valid, but can't be executed, problem thrown");

          throw new JsExecutionCanNotBeExecutedProblem("Code fragment is valid but can't be executed");
        }

        logger.debug("Validation finished. Code is valid");

      } catch (PolyglotException e) {

        logger.error("Script is invalid, problem thrown");

        if (e.isSyntaxError()) {
          logger.error(e.getSourceLocation().toString());
        }

        throw new JsExecutionCanNotBeExecutedProblem(e);
      }
    }
  }
}
