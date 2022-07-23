package com.anton.martynenko.jswrapper.jsexecution.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.jetbrains.annotations.NotNull;

/**
 * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} statuses.
 *
 * @author Martynenko Anton
 * @since 1.0
 */
public enum  Status {

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object is created.
   */
  CREATED,

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} object is submitted to pool.
   */
  SUBMITTED,

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} is cancelled.
   */
  CANCELLED,

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} is running.
   */
  RUNNING,

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} finished successfully.
   */
  SUCCESSFUL,

  /**
   * {@link com.anton.martynenko.jswrapper.jsexecution.JsExecution} finished unsuccessfully (crushed or stopped manually).
   */
  UNSUCCESSFUL;

  @Override
  public String toString() {
    return this.name();
  }


  /**
   *Need this one to serialize with no exceptions.
   * @param text Status name's string
   * @return {@link com.anton.martynenko.jswrapper.jsexecution.enums.Status}
   * @since 1.0
   */
  @JsonCreator
  public static Status fromText(final @NotNull String text) {
    return Status.valueOf(text);
  }
}
