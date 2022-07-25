package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import com.anton.martynenko.jswrapper.jsexecution.problem.EntityCanNotBeSavedProblem;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.annotation.concurrent.ThreadSafe;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * JsExecution repository layer realization based on runtime {@link HashMap}.
 * @author Martynenko Anton
 * @since 1.1
 */

@ThreadSafe
@Primary
@Repository
public final class MapJsExecutionRepository implements JsExecutionRepository {

  /**
   * Local {@link org.slf4j.Logger} bean.
   */

  private final Logger logger;

  /**
   * Static atomic generator for entity ids.
   */

  private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

  /**
   * Simple synchronized container for runtime existing entities.
   */
  private final Map<Integer, JsExecution> storage = Collections.synchronizedMap(new HashMap<>());

  /**
   * Autowiring constructor.
   * @param logger {@link Logger} to inject
   */
  @Autowired
  public MapJsExecutionRepository(@NotNull final Logger logger) {
    this.logger = logger;
  }


  /**
   * Get one entity by id or null.
   * @param id {@link JsExecution} id
   * @return {@link JsExecution} identified by id
   */
  @Nullable
  @Override
  public JsExecution getOne(@NotNull final Integer id) {
    return storage.get(id);
  }

  /**
   * Get empty or not entities collection.
   * @return Collection of all {@link JsExecution} or empty Collection
   */

  @NotNull
  @Override
  public Collection<JsExecution> findAll() {
    return findAll(null, null);
  }

  /**
   * Get empty or not entities collection with optional searching flags.
   * @param status {@link Status} (optional)
   * @param sortBy {@link SortBy} (optional)
   * @return Collection of {@link JsExecution} found with criterias, or empty Collection
   */

  @NotNull
  @Override
  public Collection<JsExecution> findAll(@Nullable final Status status, @Nullable final SortBy sortBy) {
    Collection<JsExecution> jsExecutions = storage.values();

    //filter if criteria exists

    if (status != null) {
      jsExecutions = jsExecutions.stream().filter(value -> value.getStatus().equals(status)).collect(Collectors.toList());
    }

    //sort if criteria exists

    if (sortBy != null) {
      if (sortBy.equals(SortBy.ID)) {
        jsExecutions = jsExecutions.stream().sorted(Comparator.comparing(JsExecution::getId).reversed()).collect(Collectors.toList());
      } else if (sortBy.equals(SortBy.SCHEDULED_TIME)) {
        jsExecutions = jsExecutions.stream().sorted(Comparator.comparing(JsExecution::getScheduledTime).reversed()).collect(Collectors.toList());
      }
    }

    logger.debug("With status {} and sortBy {} {} jsExecutions found", status, sortBy, jsExecutions.size());

    return jsExecutions;
  }


  /**
   * Add or update entity, set it's id and return it after that.
   * @param jsExecution new or existing {@link JsExecution}
   * @return {@link JsExecution}
   * @throws EntityCanNotBeSavedProblem if reflection is not supported end entities cannot not be saved in normal way
   */

  @NotNull
  @Override
  public JsExecution save(@NotNull final JsExecution jsExecution) throws EntityCanNotBeSavedProblem {
    if (jsExecution.getId() == 0) {
      try {
        //reflective id update
        FieldUtils.writeField(jsExecution, "id", ID_GENERATOR.incrementAndGet(), true);

      } catch (Exception e){

        throw new EntityCanNotBeSavedProblem(e);

      }
    }
    storage.put(jsExecution.getId(), jsExecution);

    return jsExecution;
  }


  /**
   * Delete entity (if exists).
   * @param jsExecution {@link JsExecution} to be deleted
   */

  @Override
  public void delete(@NotNull final JsExecution jsExecution) {

    storage.remove(jsExecution.getId());
  }


  /**
   * Delete all entities.
   */

  @Override
  public void deleteAll() {
    storage.clear();
    ID_GENERATOR.set(0);
  }
}
