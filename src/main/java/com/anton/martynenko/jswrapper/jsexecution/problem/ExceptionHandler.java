package com.anton.martynenko.jswrapper.jsexecution.problem;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/**
 * Universal exception handler.
 *
 * @author Martynenko Anton
 * @since 1.0
 */

@ControllerAdvice
public class ExceptionHandler implements ProblemHandling {
}
