package com.anton.martynenko.jswrapper.jsexecution.problem;

import org.jetbrains.annotations.NotNull;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class EntityCanNotBeSavedProblem extends AbstractThrowableProblem {

  /**
   * Custom constructor which should be used when saving of entity failed .
   * @param e {@link Exception}
   * @author Martynenko Anton
   * @since 1.1
   */
  public EntityCanNotBeSavedProblem(@NotNull final Exception e) {

    super(Problem.DEFAULT_TYPE,
        "Entity saving finished with exception",
        Status.BAD_REQUEST,
        e.toString());
    e.printStackTrace();
  }
}
