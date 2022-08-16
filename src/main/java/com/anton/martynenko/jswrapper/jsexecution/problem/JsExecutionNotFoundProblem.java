package com.anton.martynenko.jswrapper.jsexecution.problem;

import org.jetbrains.annotations.NotNull;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

/**
 * Custom {@link AbstractThrowableProblem}'s extension .
 * @author Martynenko Anton
 * @since 1.1
 * @see AbstractThrowableProblem
 */
public class JsExecutionNotFoundProblem extends AbstractThrowableProblem {

  /**
   * Custom constructor which should be used when {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} is not found.
   * @param jsExecutionId id of not found entity
   * @author Martynenko Anton
   * @since 1.1
   */
  public JsExecutionNotFoundProblem(@NotNull final Integer jsExecutionId) {
    super(Problem.DEFAULT_TYPE,
        Status.NOT_FOUND.getReasonPhrase(),
        Status.NOT_FOUND,
        String.format("JsExecution id '%d' not found", jsExecutionId));
  }
}
