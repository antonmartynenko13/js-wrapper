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

public class NoSuchPropertyProblem extends AbstractThrowableProblem {
  /**
   * Custom constructor which should be used when {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution}
   * has no requested property .
   * @param property unknown requested property
   * @author Martynenko Anton
   * @since 1.1
   */
  public NoSuchPropertyProblem(@NotNull final String property) {
    super(Problem.DEFAULT_TYPE,
        Status.BAD_REQUEST.getReasonPhrase(),
        Status.BAD_REQUEST,
        String.format("JsExecution contains no '%s' details property", property));
  }
}
