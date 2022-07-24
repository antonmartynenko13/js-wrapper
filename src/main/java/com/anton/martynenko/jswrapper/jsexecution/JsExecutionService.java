package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * JsExecution service contract.
 * @author Martynenko Anton
 * @since 1.2
 */

public interface JsExecutionService {

  /**
   * Runs new JS code execution.
   *
   * @param jsExecution new {@link JsExecution}
   * @return {@link  JsExecution} object
   * @since 1.2
   */
  @NotNull JsExecution saveAndExecute(@NotNull JsExecution jsExecution);

  /**
   * Returns all executions.
   *
   * @param status optional filtration criteria
   * @param sortBy optional sorting criteria
   * @return collection of {@link  JsExecution} objects or empty collection
   * @since 1.2
   */

  @NotNull
  Collection<JsExecution> getJsExecutions(@Nullable Status status, @Nullable SortBy sortBy);

  /**
   * Returns execution by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return {@link  JsExecution} object or null
   * @since 1.2
   */

  @NotNull
  JsExecution getJsExecution(@NotNull Integer executionId);


  /**
   * Delete execution.
   *
   * @param jsExecution {@link  JsExecution} id
   * @since 1.2
   */

  void deleteJsExecution(@NotNull JsExecution jsExecution);

  /**
   * Cancel or stop running execution.
   *
   * @param jsExecution {@link  JsExecution} id
   * @return stopped {@link  JsExecution} or null
   * @since 1.2
   */

  @NotNull
  JsExecution cancelJsExecution(@NotNull JsExecution jsExecution);
}

