package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
public final class JsExecution implements Runnable {

  /**
   * Local {@link org.slf4j.Logger} bean.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JsExecution.class);


  /**
   * Object for checking state of thread and it's result.
   */
  @GuardedBy("this")
  private Future<?> executionFuture;

  /**
   * Simple numeric positive id.
   */
  @GuardedBy("this")
  private volatile int id;

  /**
   * String code fragment.
   */

  private final String scriptBody;

  /**
   * {@link Status} of execution.
   */
  @GuardedBy("this")
  private Status status = Status.CREATED;

  /**
   * Creation time.
   */

  private final ZonedDateTime scheduledTime = ZonedDateTime.now();

  /**
   * Execution's finishing time.
   */
  @GuardedBy("this")
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
   * Exception thrown during code parsing and running.
   *
   */
  @GuardedBy("this")
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
  public void run() {
    LOGGER.info("Execution of script id {} started", this.id);

    setStatus(Status.RUNNING);
    try (Context context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .out(out)
        .err(err)

        .build()) {

      Value script = context.parse(Source.create("js", scriptBody));
      script.execute();
      setExecutionTime(ZonedDateTime.now());
      setStatus(Status.SUCCESSFUL);

      LOGGER.info("Execution of script id {} is completed successfully", this.id);

    } catch (PolyglotException pe) {

      //save exception only if execution wasn't cancelled
      if (!pe.isCancelled()) {

        LOGGER.error("Code fragment is not valid. Context will be closed. Exception information saved. Context will be closed.");
        setStatus(Status.REJECTED);
        setException(pe);
      }
    } catch (Exception e) {

      LOGGER.error("Unknown exception during code executing. Exception type is {} Context will be closed. ", e.getClass().getName());

      setStatus(Status.UNSUCCESSFUL);
    }
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
   * Setter for property 'id'.
   *
   * @param id Value to set for property 'id'.
   */
  public synchronized void setId(final int id) {
    this.id = id;
  }

  /**
   * Id getter.
   * @return current id
   */

  public synchronized int getId() {
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
  String getScheduledTime() {
    return this.scheduledTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  /**
   * ExecutionTime Jackson's getter.
   * @return stringified value of this.executionTime property with {@link DateTimeFormatter} ISO_ZONED_DATE_TIME pattern
   */
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
   * Execute script with task executor.
   * @param taskExecutor ThreadPoolTaskExecutor object
   * @since 1.1
   */

  synchronized void submitExecution(@NotNull final ThreadPoolTaskExecutor taskExecutor) {

    if (this.executionFuture != null) {

      LOGGER.debug("JsExecution with id {} is already submitted. Problem thrown.", this.id);

      throw new IllegalStateException("JsExecution can't be executed twice.");
    }

    this.executionFuture = taskExecutor.submit(this);

    setStatus(Status.SUBMITTED);

    LOGGER.debug("JsExecution id {} successfully submitted", this.id);
  }


  /**
   * Begin stopping script process and mark it as CANCELLED {@link  com.anton.martynenko.jswrapper.jsexecution.enums.Status}.
   * @return false if execution has not cancellable status
   * @since 1.1
   */

  synchronized boolean cancel() {

    if (this.executionFuture != null && this.executionFuture.cancel(true)) {
      setStatus(Status.CANCELLED);

      LOGGER.debug("JsExecution id {} cancelled successfully", this.id);

      return true;
    }

    LOGGER.debug("JsExecution id {} and status {} can't be canceled", this.id, this.status);
    return false;
  }

  /**
   * Setter for property 'exception'.
   *
   * @param exception Value to set for property 'exception'.
   */
  private synchronized void setException(final Exception exception) {
    this.exception = exception;
  }


  @NotNull
  synchronized JsExecutionDTO getDto() {
    String exceptionInfo = "";

    if (exception != null) {
      exceptionInfo = exception.getMessage();
      if (exception instanceof PolyglotException) {
        PolyglotException polyglotException = (PolyglotException) exception;

        exceptionInfo = exceptionInfo + "\n" + polyglotException.getSourceLocation();
      }
    }

    return new JsExecutionDTO(
        this.id,
        this.status,
        this.scriptBody,
        this.scheduledTime,
        this.executionTime,
        collectExecutionLog(),
        collectErrorLog(),
        this.executionFuture != null && !this.executionFuture.isDone(),
        exceptionInfo
        );
  }

  @Override
  public synchronized String toString() {
    return "JsExecution{" +
        "id=" + id +
        ", scriptBody='" + scriptBody + '\'' +
        ", status=" + status +
        ", scheduledTime=" + scheduledTime +
        ", executionTime=" + executionTime +
        '}';
  }
}
