package io.vertx.ext.web;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.impl.ParsableHeaderValue;
import io.vertx.ext.web.impl.ParsableMIMEValue;

public interface ParsedHeaderValue {

  /**
   * <quote>If no "q" parameter is present, the default weight is 1.</quote>
   */
  float DEFAULT_WEIGHT = 1;

  String STAR = "*";
  String EMPTY = new String("");// unique string object reference
  
  /**
   * Holds the unparsed value of the header.<br>
   * For the most part, this is the content before the semi-colon (";")
   */
  String value();
  /**
   * Holds the weight specified in the "q" parameter of the header.<br>
   * If the parameter is not specified, 1.0 is assumed according to 
   * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.1">rfc7231</a>
   * @return
   */
  float weight();
  
  /**
   * The value of the parameter specified by this key. Each is one of 3 things:
   * <ol>
   * <li>null &lt;- That key was not specified</li>
   * <li>ParsedHeaderValue.EMPTY (tested using ==) &lt;- The value was not specified</li>
   * <li>[Other] <- The value of the parameter</li>
   * </ol>
   * <b>Note:</b> The <code>q</code> parameter is never present.
   * @return 
   */
  @Nullable
  String getParameter(String key);
  
  /**
   * The parameters specified in this header value.
   * <b>Note:</b> The <code>q</code> parameter is never present.
   * @see {@link #getParameter(String)}
   * @return Unmodifiable Map of parameters of this header value
   */
  Map<String, String> getParameters();
  
  /**
   * Is this an allowed operation as specified by the corresponding header?
   * @return
   */
  boolean isPermitted();
  
  /**
   * Test if this header is matched by matchTry header 
   * @param matchTry The header to be matched from
   * @return true if this header represents a subset of matchTry, otherwise, false
   */
  boolean isMatchedBy(ParsableHeaderValue matchTry);
}

