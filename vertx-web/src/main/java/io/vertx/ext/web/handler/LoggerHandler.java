/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;

/**
 * <p>An access logger.
 * It supports the format syntax that generally follows the Apache HTTPD custom log format.</p>
 * <p>The data are logged itself by placing "%" directives in the pattern string which are replaced in the log file
 * by the values as follows:</p>
 * <p>
 * <table><thead><tr><th>Parameter</th><th>Description </th></tr></thead>
 * <tbody>
 * <tr><td><code>%%</code> </td><td>The percent sign.</td></tr>
 * <tr><td><code>%a</code> </td><td>Client IP-address </td></tr>
 * <tr><td><code>%A</code> </td><td>Local IP-address </td></tr>
 * <tr><td><code>%B</code> </td><td>Size of response in bytes, excluding HTTP headers.</td></tr>
 * <tr><td><code>%b</code> </td><td>Size of response in bytes, excluding HTTP headers. In CLF format, i.e. a '-' rather than a 0 when no bytes are sent.</td></tr>
 * <tr><td><code>%\{PARNAME}C</code> </td><td>The contents of cookie PARNAME in the request sent to the server.</td></tr>
 * <tr><td><code>%D</code> </td><td>The time taken to serve the request, in milliseconds.</td></tr>
 * <tr><td><code>%\{PARNAME}e</code> </td><td>The contents of the environment variable PARNAME, if exists. Otherwise, the contents of the system property PARNAME.</td></tr>
 * <tr><td><code>%h</code> </td><td>Remote host. Will log the IP address.</td></tr>
 * <tr><td><code>%H</code> </td><td>The request protocol.</td></tr>
 * <tr><td><code>%\{PARNAME}i</code> </td><td>The contents of PARNAME: header line(s) in the request sent to the server.</td></tr>
 * <tr><td><code>%m</code> </td><td>The request method.</td></tr>
 * <tr><td><code>%\{PARNAME}o</code> </td><td>The contents of PARNAME: header line(s) in the response. </td></tr>
 * <tr><td><code>%p</code> </td><td>The port of the server serving the request. It is the value of the part after ":" in the Host header value, if any, or the server port where the client connection was accepted on.</td></tr>
 * <tr><td><code>%P</code> </td><td>The <em>name of the thread</em> that serviced the request. </td></tr>
 * <tr><td><code>%q</code> </td><td>The query string (prepended with a ? if a query string exists, otherwise an empty string) </td></tr>
 * <tr><td><code>%r</code> </td><td>First line of request. Consists of Method URL+?QueryString ProtocolVersion</td></tr>
 * <tr><td><code>%s</code> </td><td>Response Status code. </td></tr>
 * <tr><td><code>%t</code> </td><td>Time the request was received in the format [18/Sep/2011:19:18:28 -0400]. The last number indicates the timezone offset from GMT.</td></tr>
 * <tr><td><code>%\{PATTERN}t</code> </td><td>The time, in the form given by format, which should be in an extended strftime(3) format (potentially localized).</td></tr>
 * <tr><td><code>%T</code> </td><td>The time taken to serve the request, in <strong>milliseconds</strong>. </td></tr>
 * <tr><td><code>%U</code> </td><td>The URL path requested, not including any query string. </td></tr>
 * <tr><td><code>%v</code> </td><td>The ServerName of the server serving the request. It is the value of the part before ":" in the Host header value, if any, or the resolved server name, or the server IP address.</td></tr>
 * <tr><td><code>%V</code> </td><td>Same as <code>%v</code>. </td></tr>
 * </tbody>
 * </table>
 * </p>
 * <p><h3>Modifiers</h3></p>
 * <p>
 * Particular items can be restricted to print only for responses with specific HTTP status codes
 * by placing a comma-separated list of status codes immediately following the "%".
 * For example, "%400,501{User-agent}i" logs User-agent on 400 errors and 501 errors only.
 * For other status codes, the literal string "-" will be logged. The status code list may
 * be preceded by a "!" to indicate negation: "%!200,304,302{Referer}i" logs Referer on all requests
 * that do not return one of the three specified codes.
 * </p>
 * <p><em>The Apache httpd modifiers "<" and ">" are not supported by Sling and currently ignored.</em></p>
 * <p><h3>Notes</h3>
 * For security reasons non-printable and other special characters in %C, %i and %o are escaped using
 * \\uhhhh sequences, where hhhh stands for the hexadecimal representation of the character's unicode value.
 * Exceptions from this rule are " and \, which are escaped by prepending a backslash,
 * and all whitespace characters, which are written in their Java-style notation (\\n, \\t, etc).
 * </p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="mailto:marcin.czeczko@gmail.com">Marcin Czeczko</a>
 */
@VertxGen
public interface LoggerHandler extends Handler<RoutingContext> {

  /**
   * Create a handler with default logging pattern
   *
   * @return the handler
   */
  static LoggerHandler create() {
    return new LoggerHandlerImpl(new LoggerHandlerOptions());
  }

  /**
   * Create a handler with the specified logging pattern
   *
   * @param options the logging options
   * @return the handler
   */
  static LoggerHandler create(LoggerHandlerOptions options) {
    return new LoggerHandlerImpl(options);
  }

}
