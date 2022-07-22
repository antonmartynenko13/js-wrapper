package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.SortBy;
import com.anton.martynenko.jswrapper.jsexecution.enums.Status;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * JsExecution repository CRUD contract.
 * @author Martynenko Anton
 * @since 1.1
 */

public interface JsExecutionRepository {

  /**
   * Get one entity by id or null.
   * @param id {@link JsExecution} id
   * @return {@link JsExecution} identified by id
   */
  @Nullable JsExecution getOne(@NotNull Integer id);

  /**
   * Get empty or not entities collection with optional searching flags.
   * @param status {@link Status} (optional)
   * @param sortBy {@link SortBy} (optional)
   * @return Collection of {@link JsExecution} found with criterias, or empty Collection
   */
  @NotNull Collection<JsExecution> findAll(@Nullable Status status, @Nullable SortBy sortBy);

  /**
   * Get empty or not entities collection.
   * @return Collection of all {@link JsExecution} or empty Collection
   */
  @NotNull Collection<JsExecution> findAll();

  /**
   * Add or update entity and return it after that.
   * @param jsExecution new or existing {@link JsExecution}
   * @return {@link JsExecution}
   */
  @NotNull JsExecution save(@NotNull JsExecution jsExecution);

  /**
   * Delete entity (if exists).
   * @param jsExecution {@link JsExecution} to be deleted
   */
  void delete(@NotNull JsExecution jsExecution);

  /**
   * Delete all entities.
   */
  void deleteAll();
}
