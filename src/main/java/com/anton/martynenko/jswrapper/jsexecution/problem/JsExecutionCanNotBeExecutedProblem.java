package com.anton.martynenko.jswrapper.jsexecution.problem;

import org.graalvm.polyglot.PolyglotException;
import org.jetbrains.annotations.NotNull;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

/**
 * {@link AbstractThrowableProblem} implementation for cases when code fragment can't be executed for different causes.
 *
 * @author Martynenko Anton
 * @since 1.1
 * @see PolyglotException
 * @see AbstractThrowableProblem
 */

public class JsExecutionCanNotBeExecutedProblem extends AbstractThrowableProblem {

  /**
   * Custom constructor which should be used when code parsing fails with {@link PolyglotException}.
   * @param e {@link PolyglotException}
   * @author Martynenko Anton
   * @since 1.1
   */
  public JsExecutionCanNotBeExecutedProblem(@NotNull final PolyglotException e) {
    super(Problem.DEFAULT_TYPE,
        "Code parsing ends with problem",
        Status.BAD_REQUEST,
        e.getMessage() + "\n" + e.getSourceLocation());
  }

  /**
   * Custom constructor which should be used when code parsing finished well but result can't be executed.
   * @param details details of problem
   * @author Martynenko Anton
   * @since 1.1
   */
  public JsExecutionCanNotBeExecutedProblem(@NotNull final String details) {
    super(Problem.DEFAULT_TYPE,
        details,
        Status.BAD_REQUEST);
  }
}
