package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


/**
 * Object representing execution of JS code fragment with details.
 * Class uses GraalJs javascript interpreter as engine
 * @see <a href="https://www.graalvm.org/javascript/">GraalVM javascript interpreter</a>
 *
 * @author Martynenko Anton
 * @since 1.0
 */

@ThreadSafe
@JsonDeserialize(using = JsExecutionDeserializer.class)
@JsonIgnoreProperties(value = { "scriptBody", "out", "err", "exception", "cancelable"}, ignoreUnknown = true)
public class JsExecution implements Callable<JsExecution> {

  /**
   * Local {@link org.slf4j.Logger} bean.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JsExecution.class);


  /**
   * Object for checking state of thread and it's result.
   */
  private Future<JsExecution> executionFuture;

  /**
   * Simple numeric positive id.
   */

  private volatile int id;

  /**
   * String code fragment.
   */

  private final String scriptBody;

  /**
   * Some string value. If there are nothing to return from execution, field be initialize with "undefined".
   */
  private String resultValue;

  /**
   * {@link Status} of execution.
   */
  private Status status = Status.CREATED;

  /**
   * Creation time.
   */

  private final ZonedDateTime scheduledTime = ZonedDateTime.now();

  /**
   * Execution's finishing time.
   */
  private ZonedDateTime  executionTime;

  /**
   * Empty stream to be filled with execution logs (using console.log() etc).
   */
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  /**
   * Empty stream to be filled with error logs (using console.err() etc).
   */
  private final ByteArrayOutputStream err = new ByteArrayOutputStream();

  /**
   * Exception which terminated execution.
   */
  private Exception exception;

  /**
   * Basic constructor.
   * @param scriptBody code fragment
   * @since 1.0
   */

  public JsExecution(@NotNull final String scriptBody) {
    this.scriptBody = scriptBody;
  }


  @Override
  public @NotNull JsExecution call() {

    LOGGER.info("Execution of script id {} started", this.id);

    setStatus(Status.RUNNING);
    try (Context context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .out(out)
        .err(err)

        .build()) {

      Value script = context.parse(Source.create("js", scriptBody));
      Value executionResult = script.execute();
      setExecutionTime(ZonedDateTime.now());
      setStatus(Status.SUCCESSFUL);
      this.resultValue = executionResult.toString();

      LOGGER.info("Execution of script id {} is completed successfully", this.id);

    } catch (Exception e) {
      //if execution wasn't cancelled
      if (!getStatus().equals(Status.CANCELLED)) {
        LOGGER.error("Exception during call method. Exception type is {} Context will be closed. Exception will be saved", e.getClass().getName());
        setStatus(Status.UNSUCCESSFUL);
        setException(e);
      }
    }
    return this;
  }

  /**
   * Getter for property 'executionFuture'.
   *
   * @return Value for property 'executionFuture'.
   */
  private synchronized Future<JsExecution> getExecutionFuture() {
    return executionFuture;
  }

  /**
   * Setter for property 'executionFuture'.
   *
   * @param executionFuture Value to set for property 'executionFuture'.
   */
  private synchronized void setExecutionFuture(final @NotNull Future<JsExecution> executionFuture) {
    this.executionFuture = executionFuture;
  }

  /**
   * Setter for property 'executionTime'.
   *
   * @param executionTime Value to set for property 'executionTime'.
   */
  private synchronized void setExecutionTime(final @NotNull ZonedDateTime executionTime) {
    this.executionTime = executionTime;
  }

  /**
   * Id getter.
   * @return current id
   */

  public int getId() {
    return id;
  }


  /**
   * ScriptBody getter.
   * @return script body string
   */

  String getScriptBody() {
    return scriptBody;
  }



  /**
   * ResultValue getter.
   * @return result value of execution ("undefined" if no result)
   */

  @JsonGetter
  public synchronized String getResultValue() {
    return resultValue;
  }

  /**
   * Status getter.
   * @return current execution {@link Status}
   */
  public synchronized Status getStatus() {
    return status;
  }

  /**
   * Setter for property 'status'.
   *
   * @param status Value to set for property 'status'.
   */
  public synchronized void setStatus(@NotNull final Status status) {
    this.status = status;
  }

  /**
   * ScheduledTime Jackson's getter.
   * @return stringified value of this.scheduledTime property with {@link DateTimeFormatter} ISO_ZONED_DATE_TIME pattern
   */
  @JsonGetter
  String getScheduledTime() {
    return this.scheduledTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  /**
   * ExecutionTime Jackson's getter.
   * @return stringified value of this.executionTime property with {@link DateTimeFormatter} ISO_ZONED_DATE_TIME pattern
   */
  @JsonGetter
  synchronized String getExecutionTime() {
    if (this.executionTime == null) {
      return null;
    }
    return this.executionTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }


  /**
   * Converts current execution log stream into string for serialization.
   * @return execution log as plain text
   * @since 1.1
   */

  String collectExecutionLog() {
    return new String(this.out.toByteArray(), StandardCharsets.UTF_8);
  }


  /**
   * It converts current error log stream into string for serialization.
   * @return error log as plain text
   * @since 1.1
   */
  String collectErrorLog() {
    return new String(this.err.toByteArray(), StandardCharsets.UTF_8);
  }


  /**
   * It converts not null exception info into plain text.
   * @return exception information
   */
  synchronized String collectExceptionInfo() {
    if (this.exception == null) {
      return "";
    }
    return ExceptionUtils.getStackTrace(exception);
  }

  /**
   * Setter for property 'exception'.
   *
   * @param exception Value to set for property 'exception'.
   */
  public void setException(@NotNull final Exception exception) {
    this.exception = exception;
  }

  /**
   * Execute script with task executor.
   * @param taskExecutor ThreadPoolTaskExecutor object
   * @since 1.1
   */

  synchronized void submitExecution(@NotNull final ThreadPoolTaskExecutor taskExecutor) {

    if (getExecutionFuture() != null) {

      LOGGER.debug("JsExecution with id {} is already submitted. Problem thrown.", this.id);

      throw new IllegalStateException("JsExecution can't be executed twice.");
    }
    setExecutionFuture(taskExecutor.submit(this));

    setStatus(Status.SUBMITTED);

    LOGGER.debug("JsExecution id {} successfully submitted", this.id);
  }

  synchronized boolean isCancelable() {
    Future<JsExecution> executionFuture = getExecutionFuture();
    return executionFuture != null && !executionFuture.isDone();
  }


  /**
   * Begin stopping script process and mark it as CANCELLED {@link  com.anton.martynenko.jswrapper.jsexecution.enums.Status}.
   * @return false if execution has not cancellable status
   * @since 1.1
   */

  synchronized boolean cancel() {

    Future<JsExecution> executionFuture = getExecutionFuture();

    if (executionFuture != null && executionFuture.cancel(true)) {
      setStatus(Status.CANCELLED);

      LOGGER.debug("JsExecution id {} cancelled successfully", this.id);

      return true;
    }

    LOGGER.debug("JsExecution id {} and status {} can't be canceled", this.id, this.status);
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    JsExecution that = (JsExecution) o;
    return Objects.equals(id, that.id) &&
        scriptBody.equals(that.scriptBody) &&
        Objects.equals(resultValue, that.resultValue) &&
        status == that.status &&
        scheduledTime.equals(that.scheduledTime) &&
        Objects.equals(executionTime, that.executionTime) &&
        out.equals(that.out) &&
        err.equals(that.err) &&
        Objects.equals(exception, that.exception);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id, scriptBody, resultValue, status, scheduledTime, executionTime, out, err, exception);
  }

  @Override
  public String toString() {
    return "JsExecution{" +
        "id=" + id +
        ", scriptBody='" + scriptBody + '\'' +
        ", resultValue='" + resultValue + '\'' +
        ", status=" + status +
        ", scheduledTime=" + scheduledTime +
        ", executionTime=" + executionTime +
        '}';
  }
}
