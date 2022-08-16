package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data transfer object representing immutable thread-safe snapshot of {@link JsExecution} object.
 * {@link JsExecution} instance is runnable and mutable. It could be mutated by itself or from quering code.
 * So when we want to serialize some state into json object we need to have snapshot, fixating consistent state of {@link JsExecution}
 * @author Martynenko Anton
 * @since 1.2
 */

@JsonDeserialize(using = JsExecutionDTODeserializer.class)
public final class JsExecutionDTO {

  /**
   * Simple integer immutable view of {@link JsExecution}'s id.
   */

  private final Integer id;

  /**
   * Immutable {@link JsExecution}'s {@link Status}.
   */

  @JsonFormat(shape = JsonFormat.Shape.STRING) //not serialize without this
  private final Status status;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized as native String in special request.
   */
  @JsonIgnore
  private final String scriptBody;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized only as parsed value in JsonGetter below.
   */
  @JsonIgnore
  private final ZonedDateTime scheduledTime;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized only as parsed value in JsonGetter below.
   */
  @JsonIgnore
  private final ZonedDateTime executionTime;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized as native String in special request.
   */
  @JsonIgnore
  private final String executionLog;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized as native String in special request.
   */
  @JsonIgnore
  private final String errorLog;

  /**
   * Has {@link JsonIgnore} annotation, because used like flag during HATEOAS linking.
   */
  @JsonIgnore
  private final boolean cancellable;

  /**
   * Has {@link JsonIgnore} annotation, because need to be serialized as native String in special request.
   */
  @JsonIgnore
  private final String exceptionInfo;

  /**
   * Base constructor.
   * @param id {@link JsExecution}'s id
   * @param status {@link JsExecution}'s status
   * @param scriptBody {@link JsExecution}'s scriptBody
   * @param scheduledTime {@link JsExecution}'s scheduledTime
   * @param executionTime {@link JsExecution}'s executionTime
   * @param executionLog {@link JsExecution}'s executionLog
   * @param errorLog {@link JsExecution}'s errorLog
   * @param cancellable boolean which shows {@link JsExecution} cancellable state
   * @param exceptionInfo {@link JsExecution}'s exceptionInfo
   */

  JsExecutionDTO(final Integer id,
                 final Status status,
                 @NotNull final String scriptBody,
                 final ZonedDateTime scheduledTime,
                 final ZonedDateTime executionTime,
                 final String executionLog,
                 final String errorLog,
                 final boolean cancellable,
                 final String exceptionInfo) {
    this.id = id;
    this.status = status;
    this.scriptBody = scriptBody;
    this.scheduledTime = scheduledTime;
    this.executionTime = executionTime;
    this.executionLog = executionLog;
    this.errorLog = errorLog;
    this.cancellable = cancellable;
    this.exceptionInfo = exceptionInfo;
  }

  /**
  * Short constructor used in post requests.
  * @param scriptBody javascript code fragment
  */

  JsExecutionDTO(@NotNull final String scriptBody) {
    this(null,
        null,
        scriptBody,
        null,
        null,
        null,
        null,
        false,
        null);
  }

  /**
   * Getter for property 'id'.
   *
   * @return Value for property 'id'.
   */
  public int getId() {
    return id;
  }

  /**
   * Getter for property 'scriptBody'.
   *
   * @return Value for property 'scriptBody'.
   */
  String getScriptBody() {
    return scriptBody;
  }

  /**
   * Getter for property 'status'.
   *
   * @return Value for property 'status'.
   */
  Status getStatus() {
    return status;
  }

  /**
   * Getter for property 'scheduledTime'.
   *
   * @return Value for property 'scheduledTime'.
   */
  ZonedDateTime getScheduledTime() {
    return scheduledTime;
  }

  /**
   * ExecutionTime Jackson's getter.
   * @return stringified value of this.executionTime property with {@link DateTimeFormatter} ISO_ZONED_DATE_TIME pattern
   */
  @JsonGetter
  @JsonProperty("Scheduled at")
  String getScheduledTimeString() {
    if (this.scheduledTime == null) {
      return null;
    }
    return this.scheduledTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  /**
   * Getter for property 'executionTime'.
   *
   * @return Value for property 'executionTime'.
   */
  ZonedDateTime getExecutionTime() {
    return executionTime;
  }

  /**
   * ExecutionTime Jackson's getter.
   * @return stringified value of this.executionTime property with {@link DateTimeFormatter} ISO_ZONED_DATE_TIME pattern
   */
  @JsonGetter
  @JsonProperty("Executed at")
  String getExecutionTimeString() {
    if (this.executionTime == null) {
      return null;
    }
    return this.executionTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  /**
   * Getter for property 'executionLog'.
   *
   * @return Value for property 'executionLog'.
   */
  String getExecutionLog() {
    return executionLog;
  }

  /**
   * Getter for property 'errorLog'.
   *
   * @return Value for property 'errorLog'.
   */
  String getErrorLog() {
    return errorLog;
  }

  /**
   * Getter for property 'cancellable'.
   *
   * @return Value for property 'cancellable'.
   */
  boolean isCancellable() {
    return cancellable;
  }

  /**
   * Getter for property 'exceptionInfo'.
   *
   * @return Value for property 'exceptionInfo'.
   */

  String getExceptionInfo() {
    return exceptionInfo;
  }

  @Override
  public String toString() {
    return "JsExecutionDTO{" +
        "id=" + id +
        ", status=" + status +
        ", scriptBody='" + scriptBody + '\'' +
        ", scheduledTime=" + scheduledTime +
        ", executionTime=" + executionTime +
        ", executionLog='" + executionLog + '\'' +
        ", errorLog='" + errorLog + '\'' +
        ", cancellable=" + cancellable +
        ", exceptionInfo='" + exceptionInfo + '\'' +
        '}';
  }
}
