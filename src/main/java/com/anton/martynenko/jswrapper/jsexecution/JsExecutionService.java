package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionCanNotBeCancelledProblem;
import com.anton.martynenko.jswrapper.jsexecution.problem.JsExecutionNotFoundProblem;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer component for JS code executions business logic.
 *
 * @author Martynenko Anton
 * @since 1.0
 */
@Slf4j
@Service
@ThreadSafe
public class JsExecutionService {

  /**
   * Thread-safe {@link List} storage. For some complicated operations is synched by itself in service methods.
   */

  @GuardedBy("storage")
  private final List<JsExecution> storage;

  /**
   * Configured {@link ThreadPoolTaskExecutor} bean.
   */

  private final ThreadPoolTaskExecutor taskExecutor;

  /**
   * Simple {@link JsExecutionFactory} bean.
   * Component is stateless
   */
  private final JsExecutionFactory jsExecutionFactory;

  @Autowired
  private JsExecutionService(final ThreadPoolTaskExecutor taskExecutor,
                             final JsExecutionFactory jsExecutionFactory,
                             final List<JsExecution> storage) {
    this.taskExecutor = taskExecutor;
    this.jsExecutionFactory = jsExecutionFactory;
    this.storage = storage;
  }

  @NotNull
  JsExecutionDTO createAndRun(@NotNull final JsExecutionDTO jsExecutionDTO) {
    JsExecution jsExecution = jsExecutionFactory.createNew(jsExecutionDTO.getScriptBody());


    //this fragment is a one of reasons why we can't use CopyOnWriteList
    synchronized (storage) {
      storage.add(jsExecution);
      jsExecution.setId(storage.size() - 1);
    }

    jsExecution.submitExecution(taskExecutor);

    //return immutable thread-safe serializable DTO, method is synchronized
    return jsExecution.getDto();
  }

  @NotNull
  Collection<JsExecutionDTO> findAll( @NotNull final Optional<Status> status, @NotNull final Optional<SortBy> sortBy) {
    final List<JsExecutionDTO> jsExecutionDTOList = new ArrayList<>();

    //synchronize because storage is sharing resource

    synchronized (storage) {

      /*
      * Well, I had to avoid  storage.forEach(lambda); because it's a bit confusing
      * In java 8 synchronizedList spec there are point about manual locking map for iteration
      * but their realization of Collections.synchronizedList.foreEach is actually synchronized
      * it's amazing but not pretty sure it's safe to use because of specification boundaries
      * so I used traditional foreEach because of testing reazons(
      */

      for (JsExecution jsExecution: storage) {
        if (jsExecution != null) {

          // convert to DTO because jsExecution is active sharing resource and we need it's immutable invariant to sort and serialize
          jsExecutionDTOList.add(jsExecution.getDto());
        }
      }
    }


    //next operations are thread safe because use immutable DTO's and local thread's collection
    //filter if criteria exists

    List<JsExecutionDTO> filtered = jsExecutionDTOList;


    if (status.isPresent()) {
      filtered = jsExecutionDTOList.stream()
          .filter(value -> value.getStatus().equals(status.get()))
          .collect(Collectors.toList());
    }

    //sort if criteria exists

    if (sortBy.isPresent()) {
      if (sortBy.get().equals(SortBy.ID)) {
        filtered = filtered.stream().sorted(Comparator.comparing(JsExecutionDTO::getId).reversed()).collect(Collectors.toList());
      } else if (sortBy.get().equals(SortBy.SCHEDULED_TIME)) {
        filtered = filtered.stream().sorted(Comparator.comparing(JsExecutionDTO::getScheduledTime).reversed()).collect(Collectors.toList());
      }
    }

    log.debug("With status {} and sortBy {} {} jsExecutions found", status, sortBy, filtered.size());

    return filtered;
  }

  @NotNull
  JsExecutionDTO getOne(final int executionId) {

    JsExecution jsExecution = getJsExecution(executionId);

    return jsExecution.getDto();
  }

  @NotNull
  JsExecutionDTO cancelExecution(final int executionId) {

    final JsExecution jsExecution = getJsExecution(executionId);


    synchronized (jsExecution) {
      if (!jsExecution.cancel()) {
        throw new JsExecutionCanNotBeCancelledProblem(
            String.format("JsExecution id %d and status %s can't be canceled",
                jsExecution.getId(),
                jsExecution.getStatus().name())
        );
      }

      /*
     I know that jsExecution.cancellable can be changed only from 'true' to 'false'
     and synchronisation of these line is useless, but let it be here in the name of possible flow updates
    */
      return jsExecution.getDto();
    }
  }

  void deleteExecution(final int executionId) {

    JsExecution jsExecution;

    synchronized (storage) {

      if (executionId >= storage.size()) {

        throw new JsExecutionNotFoundProblem(executionId);

      }
        jsExecution = storage.set(executionId, null);
    }

    if (jsExecution == null) {
      throw new JsExecutionNotFoundProblem(executionId);
    }

    // cancel if running or in queue
    jsExecution.cancel();
  }

  @NotNull
  private JsExecution getJsExecution(final int executionId) {

    JsExecution jsExecution;

    synchronized (storage) {

      if (executionId >= storage.size()) {

        throw new JsExecutionNotFoundProblem(executionId);

      }

      jsExecution = storage.get(executionId);
    }

    if (jsExecution == null) {

      throw new JsExecutionNotFoundProblem(executionId);

    }

    return jsExecution;
  }
}
