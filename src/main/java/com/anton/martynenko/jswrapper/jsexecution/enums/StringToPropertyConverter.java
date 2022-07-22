package com.anton.martynenko.jswrapper.jsexecution.enums;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

/**
 * Implementation of {@link Converter} for {@link Property} enums.
 *
 * @author Martynenko Anton
 * @since 1.2
 */
public class StringToPropertyConverter implements Converter<String, Property> {

  /**
   * Converts String to {@link Property} enum. We need it to use {@link Property} as path variable so we just uppercase String.
   *
   * @author Martynenko Anton
   * @since 1.2
   */
  @Override
  public Property convert(@NotNull final String source) {
    return Property.valueOf(source.toUpperCase());
  }
}
