package io.vertx.ext.web;

import java.util.Collection;
import java.util.Map;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen(concrete = false)
public interface ParsedHeaderValue {

  /**
   * <quote>If no "q" parameter is present, the default weight is 1.</quote>
   */
  float DEFAULT_WEIGHT = 1;

  /**
   * Contains the raw value that was received from the user agent
   */
  String rawValue();

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
  String parameter(String key);

  /**
   * The parameters specified in this header value.
   * <b>Note:</b> The <code>q</code> parameter is never present.
   * @see {@link #parameter(String)}
   * @return Unmodifiable Map of parameters of this header value
   */
  Map<String, String> parameters();

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
  boolean isMatchedBy(ParsedHeaderValue matchTry);

  /**
   * Finds the first ParsedHeaderValue in the list that matches with this header value.
   * Will return an empty Optional if none match.
   * <p/>
   * This method is intended for internal usage.
   *
   * @param matchTries A list of parsed headers to match from this header value
   * @return Optional potentially with the first matched header
   */
  @GenIgnore
  <T extends ParsedHeaderValue> T findMatchedBy(Collection<T> matchTries);

  /**
   * An integer that represents the absolute order position of this header
   */
  int weightedOrder();
}

