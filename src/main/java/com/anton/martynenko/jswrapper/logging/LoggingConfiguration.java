package com.anton.martynenko.jswrapper.logging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Member;
import java.util.Optional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Logging component configuration class.
 *
 * @author Martynenko Anton
 * @since 1.1
 */

@Configuration
public class LoggingConfiguration {

  /**
   * Initializes {@link Logger} bean and bind it to name of class where it is injected.
   * @param ip AOP {@link InjectionPoint}
   * @return {@link Logger}
   * @author Martynenko Anton
   * @since 1.1
   */
  @Bean
  @Scope(value = SCOPE_PROTOTYPE)
  public Logger logger(@NotNull final InjectionPoint ip) {

    return LoggerFactory.getLogger(Optional.of(ip.getMember())
        .map(Member::getDeclaringClass)
        .orElseThrow(IllegalArgumentException::new));
  }


}
