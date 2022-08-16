package com.anton.martynenko.jswrapper.jsexecution.constants;

/**
 * Properties of jsExecution and it's endpoints.
 *
 * @author Martynenko Anton
 * @since 1.1
 */
public final class Property {

  /**
   * Hidden empty constructor to forbid instance's creation .
   */
  private Property() {
  }

  /**
   * <code>jsexecution/{id}/scriptbody</code> endpoint.
   */
  public static final String SCRIPT_BODY = "scriptbody";

  /**
   * <code>jsexecution/{id}/executionlog</code> endpoint.
   */
  public static final String EXECUTION_LOG = "executionlog";

  /**
   * <code>jsexecution/{id}/errorlog</code> endpoint.
   */
  public static final String ERROR_LOG = "errorlog";

  /**
   * <code>jsexecution/{id}/exceptioninfo</code> endpoint.
   */
  public static final String EXCEPTION_INFO = "exceptioninfo";
}
