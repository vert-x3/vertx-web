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
import java.util.List
/**
 * A container with the request's headers that are meaningful enough to be parsed
 * Contains:
 * <ul>
 * <li>Accept -> MIME header, parameters and sortable</li>
 * <li>Accept-Charset -> Parameters and sortable</li>
 * <li>Accept-Encoding -> Parameters and sortable</li>
 * <li>Accept-Language -> Parameters and sortable</li>
 * <li>Content-Type -> MIME header and parameters</li>
 * </ul>
 *
*/
@CompileStatic
public class ParsedHeaderValues {
  private final def io.vertx.ext.web.ParsedHeaderValues delegate;
  public ParsedHeaderValues(Object delegate) {
    this.delegate = (io.vertx.ext.web.ParsedHeaderValues) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * @return List of MIME values in the <code>Accept</code> header
   */
  public List<MIMEHeader> accept() {
    def ret = (List)delegate.accept()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.MIMEHeader.class)});
    return ret;
  }
  /**
   * @return List of charset values in the <code>Accept-Charset</code> header
   */
  public List<ParsedHeaderValue> acceptCharset() {
    def ret = (List)delegate.acceptCharset()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.ParsedHeaderValueImpl.class)});
    return ret;
  }
  /**
   * @return List of encofing values in the <code>Accept-Encoding</code> header
   */
  public List<ParsedHeaderValue> acceptEncoding() {
    def ret = (List)delegate.acceptEncoding()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.ParsedHeaderValueImpl.class)});
    return ret;
  }
  /**
   * @return List of languages in the <code>Accept-Language</code> header
   */
  public List<LanguageHeader> acceptLanguage() {
    def ret = (List)delegate.acceptLanguage()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.LanguageHeader.class)});
    return ret;
  }
  /**
   * @return MIME value in the <code>Content-Type</code> header
   */
  public MIMEHeader contentType() {
    def ret = InternalHelper.safeCreate(delegate.contentType(), io.vertx.groovy.ext.web.MIMEHeader.class);
    return ret;
  }
}
