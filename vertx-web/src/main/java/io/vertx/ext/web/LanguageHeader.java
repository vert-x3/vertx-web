package io.vertx.ext.web;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A parsed language header.
 * Delivers a more direct access to the individual elements of the header it represents
 */
@VertxGen
public interface LanguageHeader extends ParsedHeaderValue, Locale{

  /**
   * The tag of the language as specified by
   * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>.<br>
   * Equivalent to {@link #subtag(int) subtag(0)}
   * @return The language tag
   */
  String tag();
  /**
   * The subtag of the language as specified by
   * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>.<br>
   * Equivalent to {@link #subtag(int) subtag(1)}
   * @return The language subtag
   */
  @Nullable String subtag();
  /**
   * A subtag of this language header.<br>
   * + info: <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>
   *
   * @return The language subtag at specified position
   */
  @Nullable String subtag(int level);

  /**
   * @return the number of subtags this value has
   */
  int subtagCount();
}

