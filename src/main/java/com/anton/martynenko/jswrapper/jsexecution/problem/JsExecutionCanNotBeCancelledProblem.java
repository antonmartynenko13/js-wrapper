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

public class JsExecutionCanNotBeCancelledProblem extends AbstractThrowableProblem {
  /**
   * Custom constructor which should be used when code trying to cancel not cancelable object .
   * @param details details of problem
   * @author Martynenko Anton
   * @since 1.1
   */
  public JsExecutionCanNotBeCancelledProblem(@NotNull final String details) {
    super(Problem.DEFAULT_TYPE,
        Status.METHOD_NOT_ALLOWED.getReasonPhrase(),
        Status.METHOD_NOT_ALLOWED,
        details);
  }
}
