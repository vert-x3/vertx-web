package io.vertx.ext.web;

import java.util.Map;

import io.vertx.codegen.annotations.Nullable;

public interface LanguageHeader extends ParsedHeaderValue, Locale{
  
  /**
   * The tag of the language as specified by 
   * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>.<br>
   * Equivalent to {@link #subtag(int) subtag(0)}
   * @return
   */
  String tag();
  /**
   * The subtag of the language as specified by 
   * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>.<br>
   * Equivalent to {@link #subtag(int) subtag(1)}
   * @return
   */
  @Nullable
  String subtag();
  /**
   * A subtag of this language header.<br>
   * + info: <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">rfc7231#section-3.1.3.1</a>
   * 
   * @return
   */
  @Nullable
  String subtag(int level);
  
  /**
   * The number of subtags this value has.
   */
  int subtagCount();
}

