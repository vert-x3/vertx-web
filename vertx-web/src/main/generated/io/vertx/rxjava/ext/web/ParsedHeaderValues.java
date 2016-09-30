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

package io.vertx.rxjava.ext.web;

import java.util.Map;
import rx.Observable;
import java.util.List;

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
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.ParsedHeaderValues original} non RX-ified interface using Vert.x codegen.
 */

public class ParsedHeaderValues {

  final io.vertx.ext.web.ParsedHeaderValues delegate;

  public ParsedHeaderValues(io.vertx.ext.web.ParsedHeaderValues delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public List<MIMEHeader> accept() { 
    List<MIMEHeader> ret = delegate.accept().stream().map(elt -> MIMEHeader.newInstance(elt)).collect(java.util.stream.Collectors.toList());
    return ret;
  }

  public List<ParsedHeaderValue> acceptCharset() { 
    List<ParsedHeaderValue> ret = delegate.acceptCharset().stream().map(elt -> ParsedHeaderValue.newInstance(elt)).collect(java.util.stream.Collectors.toList());
    return ret;
  }

  public List<ParsedHeaderValue> acceptEncoding() { 
    List<ParsedHeaderValue> ret = delegate.acceptEncoding().stream().map(elt -> ParsedHeaderValue.newInstance(elt)).collect(java.util.stream.Collectors.toList());
    return ret;
  }

  public List<LanguageHeader> acceptLanguage() { 
    List<LanguageHeader> ret = delegate.acceptLanguage().stream().map(elt -> LanguageHeader.newInstance(elt)).collect(java.util.stream.Collectors.toList());
    return ret;
  }

  public MIMEHeader contentType() { 
    MIMEHeader ret = MIMEHeader.newInstance(delegate.contentType());
    return ret;
  }


  public static ParsedHeaderValues newInstance(io.vertx.ext.web.ParsedHeaderValues arg) {
    return arg != null ? new ParsedHeaderValues(arg) : null;
  }
}
