/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import java.util.Map
@CompileStatic
public interface ParsedHeaderValue {
  public Object getDelegate();
  String rawValue();
  String value();
  float weight();
  String getParameter(String key);
  Map<String,String> getParameters();
  boolean isPermitted();
  boolean isMatchedBy(ParsedHeaderValue matchTry);
  int weightedOrder();
}

@CompileStatic
class ParsedHeaderValueImpl implements ParsedHeaderValue {
  private final def io.vertx.ext.web.ParsedHeaderValue delegate;
  public ParsedHeaderValueImpl(Object delegate) {
    this.delegate = (io.vertx.ext.web.ParsedHeaderValue) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Contains the raw value that was received from the user agent 
   * @return 
   */
  public String rawValue() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).rawValue();
    return ret;
  }
  /**
   * Holds the unparsed value of the header.<br>
   * For the most part, this is the content before the semi-colon (";")
   * @return 
   */
  public String value() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).value();
    return ret;
  }
  /**
   * Holds the weight specified in the "q" parameter of the header.<br>
   * If the parameter is not specified, 1.0 is assumed according to 
   * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.1">rfc7231</a>
   * @return 
   */
  public float weight() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).weight();
    return ret;
  }
  /**
   * The value of the parameter specified by this key. Each is one of 3 things:
   * <ol>
   * <li>null &lt;- That key was not specified</li>
   * <li>ParsedHeaderValue.EMPTY (tested using ==) &lt;- The value was not specified</li>
   * <li>[Other] <- The value of the parameter</li>
   * </ol>
   * <b>Note:</b> The <code>q</code> parameter is never present.
   * @param key 
   * @return 
   */
  public String getParameter(String key) {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).getParameter(key);
    return ret;
  }
  /**
   * The parameters specified in this header value.
   * <b>Note:</b> The <code>q</code> parameter is never present.
   * @return Unmodifiable Map of parameters of this header value
   */
  public Map<String, String> getParameters() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).getParameters();
    return ret;
  }
  /**
   * Is this an allowed operation as specified by the corresponding header?
   * @return 
   */
  public boolean isPermitted() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).isPermitted();
    return ret;
  }
  /**
   * Test if this header is matched by matchTry header 
   * @param matchTry The header to be matched from
   * @return true if this header represents a subset of matchTry, otherwise, false
   */
  public boolean isMatchedBy(ParsedHeaderValue matchTry) {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).isMatchedBy(matchTry != null ? (io.vertx.ext.web.ParsedHeaderValue)matchTry.getDelegate() : null);
    return ret;
  }
  /**
   * An integer that represents the absolute order position of this header
   * @return 
   */
  public int weightedOrder() {
    def ret = ((io.vertx.ext.web.ParsedHeaderValue) delegate).weightedOrder();
    return ret;
  }
}
