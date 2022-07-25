package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.graalvm.GraalVmHelper;
import com.anton.martynenko.jswrapper.graalvm.Language;
import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeExecutedProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service layer component for JS code executions business logic.
 *
 * @author Martynenko Anton
 * @since 1.0
 */
@Primary
@Service
public final class JsExecutionServiceImpl implements JsExecutionService{

  /**
   * Local {@link org.slf4j.Logger} bean.
   */

  private final Logger logger;

  /**
   * {@link JsExecutionRepository} repository bean.
   * */

  private final JsExecutionRepository jsExecutionRepository;

  /**
   * Configured {@link ThreadPoolTaskExecutor} bean.
   */

  private final ThreadPoolTaskExecutor taskExecutor;


  /**
   * {@link GraalVmHelper} bean.
   */

  private final GraalVmHelper graalVmHelper;

  /**
   * Autowiring constructor.
   * @param logger {@link Logger} bean
   * @param mapJsExecutionRepository {@link MapJsExecutionRepository} bean
   * @param taskExecutor {@link ThreadPoolTaskExecutor} bean
   * @param graalVmHelper {@link GraalVmHelper} bean
   */
  @Autowired
  public JsExecutionServiceImpl(@NotNull final Logger logger,
                                @NotNull final JsExecutionRepository mapJsExecutionRepository,
                                @NotNull final ThreadPoolTaskExecutor taskExecutor,
                                @NotNull final GraalVmHelper graalVmHelper) {
    this.logger = logger;
    this.jsExecutionRepository = mapJsExecutionRepository;
    this.taskExecutor = taskExecutor;
    this.graalVmHelper = graalVmHelper;
  }

  /**
   * Save and run new JS code execution.
   *
   * @param jsExecution new {@link JsExecution}
   * @return {@link  JsExecution} object
   * @throws JsExecutionCanNotBeExecutedProblem when JsExecution.scriptBody can't be executed
   * @since 1.0
   */
  @NotNull
  @Override
  public JsExecution saveAndExecute(@NotNull JsExecution jsExecution) throws JsExecutionCanNotBeExecutedProblem{

    graalVmHelper.validate(jsExecution.getScriptBody(), Language.JS);

    jsExecution.submitExecution(taskExecutor);

    jsExecution = jsExecutionRepository.save(jsExecution);

    return jsExecution;
  }

  /**
   * Returns all executions.
   *
   * @param status optional filtration criteria
   * @param sortBy optional sorting criteria
   * @return collection of {@link  JsExecution} objects or empty collection
   * @since 1.0
   */

  @NotNull
  @Override
  public  Collection<JsExecution> getJsExecutions(@Nullable final Status status, @Nullable final SortBy sortBy) {

    return jsExecutionRepository.findAll(status, sortBy);
  }

  /**
   * Returns execution by id.
   *
   * @param executionId {@link  JsExecution} id
   * @return  {@link  JsExecution} object or null
   * @throws JsExecutionNotFoundProblem when no {@link JsExecution} with such id
   * @since 1.0
   */

  @NotNull
  @Override
  public JsExecution getJsExecution(@NotNull final Integer executionId) throws JsExecutionNotFoundProblem {
    JsExecution jsExecution = jsExecutionRepository.getOne(executionId);
    if (jsExecution == null) {
      throw new JsExecutionNotFoundProblem(executionId);
    }

    return jsExecution;
  }


  /**
   * Delete execution.
   *
   * @param jsExecution {@link  JsExecution} id
   *
   * @since 1.0
   */

  @Override
  public void deleteJsExecution(@NotNull final JsExecution jsExecution) {

    jsExecution.cancel();
    jsExecutionRepository.delete(jsExecution);
  }

  /**
   * Cancel or stop running execution.
   *
   * @param jsExecution {@link  JsExecution} id
   * @return cancelled {@link  JsExecution}
   * @throws JsExecutionCanNotBeCancelledProblem if execution can't be cancelled
   *
   * @since 1.0
   */

  @NotNull
  @Override
  public JsExecution cancelJsExecution(@NotNull final JsExecution jsExecution) throws JsExecutionCanNotBeCancelledProblem {
    if (!jsExecution.cancel()) {
      throw new JsExecutionCanNotBeCancelledProblem(String.format("JsExecution with status %s cant be cancelled.", jsExecution.getStatus()));
    }

    return jsExecution;
  }
}
