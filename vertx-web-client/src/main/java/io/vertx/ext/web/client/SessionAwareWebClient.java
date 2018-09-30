/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.ext.web.client.impl.SessionAwareWebClientImpl;

/**
 * An asynchronous sessions aware HTTP / HTTP/2 client called {@code SessionAwareWebClient}.
 * <p>
 * This client wraps a {@link WebClient} and makes it session aware adding features to it:
 * <ul>
 *   <li>Per client headers, to be send with every request</li>
 *   <li>Per client cookies, to be send with every request</li>
 *   <li>Automatic storage and sending of cookies received from the server(s)</li>
 * </ul>
 * <p>
 * The client honors the cookies attributes:
 * <ul>
 *  <li>domain</li>
 *  <li>path</li>
 *  <li>secure</li>
 *  <li>max-age and expires</li>
 * </ul>
 *
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public interface SessionAwareWebClient extends WebClient {

  /**
   * Create a session aware web client using the provided {@code webClient} instance.
   * 
   * @param webClient the web client instance
   * @return the created client
   */
  static SessionAwareWebClient build(WebClient webClient) {
    return build(webClient, CookieStore.build());
  }

  /**
   * Create a session aware web client using the provided {@code webClient} instance.
   * 
   * @param webClient the web client instance
   * @return the created client
   */
  static SessionAwareWebClient build(WebClient webClient, CookieStore cookieStore) {
    return new SessionAwareWebClientImpl(webClient, cookieStore);
  }

  /**
   * Configure the client to add an HTTP header o every request.
   *
   * @param name the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient setHeader(CharSequence name, CharSequence value);

  /**
   * Configure the client to add an HTTP header o every request.
   *
   * @param name the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient setHeader(String name, String value);

  /**
   * Configure the client to add an HTTP header o every request.
   *
   * @param name the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient setHeader(CharSequence name, Iterable<CharSequence> values);

  /**
   * Configure the client to add an HTTP header o every request.
   *
   * @param name the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient setHeader(String name, Iterable<String> values);

  /**
   * Removes a previously added header.
   * 
   * @param name the header name
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient removeHeader(CharSequence name);

  /**
   * Removes a previously added header.
   * 
   * @param name the header name
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SessionAwareWebClient removeHeader(String name);

  /**
   * Returns this client's {@code CookieStore}
   * <p>
   * All cookies added to this store will be send with every request.
   * The CookieStore honors the domain, path, secure and max-age properties of received cookies
   * and is automatically updated with cookies present in responses received by this client.  
   * @return this client's cookie store
   */
  CookieStore getCookieStore();
}
