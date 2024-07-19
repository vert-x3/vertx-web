/*
 * Copyright 2024 Red Hat, Inc.
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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A builder for {@link CSPHandler} instances.
 */
@VertxGen
public interface CSPHandlerBuilder {

  /**
   * Sets a single directive entry to the handler. All previously set or added directives will be replaced.
   * For more information on directives see: <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy">Content-Security-Policy</a>.
   *
   * @param name the directive name
   * @param value the directive value.
   * @return fluent self
   */
  @Fluent
  CSPHandlerBuilder setDirective(String name, String value);

  /**
   * Adds a single directive entry to the handler. All previously set or added directives will be preserved.
   * For more information on directives see: <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy">Content-Security-Policy</a>.
   *
   * @param name the directive name
   * @param value the directive value.
   * @return fluent self
   */
  @Fluent
  CSPHandlerBuilder addDirective(String name, String value);

  /**
   * To ease deployment, CSP can be deployed in report-only mode. The policy is not enforced, but any violations are
   * reported to a provided URI. Additionally, a report-only header can be used to test a future revision to a policy
   * without actually deploying it.
   *
   * @param reportOnly enable report only
   * @return fluent self.
   */
  @Fluent
  CSPHandlerBuilder reportOnly(boolean reportOnly);

  /**
   * @return a new instance of {@link CSPHandler}
   */
  CSPHandler build();
}
