package com.anton.martynenko.jswrapper;

import com.anton.martynenko.jswrapper.jsexecution.JsExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Main class used to configure, run spring boot application, and prepare beans.
 *
 * @author Martynenko Anton
 * @since 1.0
 */

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableAsync
public class JsWrapperServiceApplication implements WebMvcConfigurer {
  /**
   * Simple minimal configured {@link Logger}.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JsWrapperServiceApplication.class);

  /**
   * Runtime thread-safe executions object storage.
   * @return synchronizedList
   */
  @Bean
  public List<JsExecution> storage() {
    return Collections.synchronizedList(new ArrayList<>());
  }

  /**
   * Prepare basic Jackson mapper to be injected as bean.
   * @return Jackson's {@link  com.fasterxml.jackson.databind.ObjectMapper}
   */

  @Bean
  public ObjectMapper objectMapper() {
    LOGGER.info("Jackson's object mapper insitialization...");

    return new ObjectMapper().registerModules(
      new ProblemModule(),
      new ConstraintViolationProblemModule(),
      new JavaTimeModule());
  }


  /**
   * Prepare Spring Boot embedded task executor to be injected as bean.
   * @return {@link  org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor} entity
   */

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    LOGGER.info("ThreadPoolTaskExecutor initialization... ");
    //We can inject executor without declaring here, but we need some configuration

    // get the runtime object associated with the current Java application
    Runtime runtime = Runtime.getRuntime();

    // get the number of processors available to the Java virtual machine
    int numberOfProcessors = runtime.availableProcessors();

    LOGGER.info("Number of processors available: {}", numberOfProcessors);

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(numberOfProcessors);
    executor.setMaxPoolSize(numberOfProcessors);
    return executor;
  }

  /**
   * Application entry point.
   * @param args arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(JsWrapperServiceApplication.class, args);
  }

}
