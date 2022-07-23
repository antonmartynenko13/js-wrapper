package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Object representing execution of JS code fragment with details.
 * Class uses GraalJs javascript interpreter as engine
 * @see <a href="https://www.graalvm.org/javascript/">GraalVM javascript interpreter</a>
 *
 * @author Martynenko Anton
 * @since 1.0
 */

@ThreadSafe
@JsonIgnoreProperties(value = { "taskExecutor", "scriptBody", "out", "err", "context", "exception"}, ignoreUnknown = true)
public class JsExecution extends RepresentationModel<JsExecution> implements Callable<JsExecution> {

  /**
   * Local {@link org.slf4j.Logger} bean.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JsExecution.class);


  /**
   * Object for checking state of thread and it's result.
   */

  private Future<JsExecution> result;

  /**
   * Simple numeric positive id.
   */

  private Integer id;

  /**
   * String code fragment.
   */

  private String scriptBody;

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

  /**
   * Initializing of json representation with hateoas endpoints.
   * Must be invoked in the end of construction
   * @since 1.1
   */
  private void initializeRepresentationModel() {
    for (Property property: Property.values()) {
      Link link = linkTo(methodOn(JsExecutionController.class)
          .getExecutionDetails(this.id, property)).withRel(property.name());
      this.add(link);
    }
    LOGGER.debug("RepresentationModel initialized with HATEOAS properties : {}", Arrays.toString(Property.values()));
  }


  @Override
  public @NotNull JsExecution call() {

    LOGGER.info("Execution of script id {} started", this.id);

    this.status = Status.RUNNING;
    try (Context context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .out(out)
        .err(err)

        .build()) {

      Value script = context.parse(Source.create("js", scriptBody));
      Value executionResult = script.execute();
      executionTime = ZonedDateTime.now();
      this.status = Status.SUCCESSFUL;
      this.resultValue = executionResult.toString();

      LOGGER.info("Execution of script id {} is completed successfully", this.id);

    } catch (Exception e) {

      LOGGER.error("Exception during call method. Exception type is {} Context will be closed. Exception will be saved", e.getClass().getName());

      this.status = Status.UNSUCCESSFUL;
      this.exception = e;
    }
    return this;
  }


  /**
   * Id getter.
   * @return current id
   */

  public synchronized Integer getId() {
    return id;
  }

  /**
   * Id private setter. Could be used only in repository or tests(with Reflection API)
   * @param id positive, not null id to set
   */
  private synchronized void setId(@NotNull final Integer id) {
    this.id = id;
    initializeRepresentationModel();
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
  public String getResultValue() {
    return resultValue;
  }

  /**
   * Status getter.
   * @return current execution {@link Status}
   */
  public Status getStatus() {
    return status;
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
  String getExecutionTime() {
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
  String collectExceptionInfo() {
    if (this.exception == null) {
      return "";
    }
    return ExceptionUtils.getStackTrace(exception);
  }

  /**
   * Execute script with task executor.
   * @param taskExecutor ThreadPoolTaskExecutor object
   * @return {@link java.util.concurrent.Future} object for result retrieving
   * @since 1.1
   */

  synchronized Future<JsExecution> submitExecution(@NotNull final ThreadPoolTaskExecutor taskExecutor) {

    if (!this.status.equals(Status.CREATED)) {

      LOGGER.debug("JsExecution with id {} and status {} status is trying to be submitted. Problem thrown.", this.id, this.status);

      throw new IllegalStateException(String.format("JsExecution with status %s can't be executed", this.status));
    }

    this.result = taskExecutor.submit(this);

    this.status = Status.SUBMITTED;

    LOGGER.debug("JsExecution id {} successfully submitted", this.id);

    return this.result;
  }

  /**
   * Begin stopping script process and mark it as CANCELLED {@link  com.anton.martynenko.jswrapper.jsexecution.enums.Status}.
   * @since 1.1
   */

  synchronized void stop() {

    if (this.status.equals(Status.SUBMITTED) || this.status.equals(Status.RUNNING)) {
      if (this.result != null && !this.result.isCancelled()) {
        this.result.cancel(true);

      }
      this.status = Status.CANCELLED; // this one will be changed to UNSUCCESSFUL if it already run

      LOGGER.debug("JsExecution id {} stopping initiated successfully", this.id);
    } else {
      LOGGER.debug("JsExecution id {} and status {} can't be stopped", this.id, this.status);
    }
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
}
