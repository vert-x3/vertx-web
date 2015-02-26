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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.core.Handler
/**
 * A handler which gathers the entire request body and sets it on the {@link io.vertx.ext.apex.RoutingContext}.
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class BodyHandler {
  final def io.vertx.ext.apex.handler.BodyHandler delegate;
  public BodyHandler(io.vertx.ext.apex.handler.BodyHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a body handler with defaults
   *
   * @return the body handler
   */
  public static BodyHandler create() {
    def ret= BodyHandler.FACTORY.apply(io.vertx.ext.apex.handler.BodyHandler.create());
    return ret;
  }
  /**
   * Set the maximum body size -1 means unlimited
   *
   * @param bodyLimit  the max size
   * @return reference to this for fluency
   */
  public BodyHandler setBodyLimit(long bodyLimit) {
    def ret= BodyHandler.FACTORY.apply(this.delegate.setBodyLimit(bodyLimit));
    return ret;
  }
  /**
   * Set the uploads directory to use
   *
   * @param uploadsDirectory  the uploads directory
   * @return reference to this for fluency
   */
  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
    def ret= BodyHandler.FACTORY.apply(this.delegate.setUploadsDirectory(uploadsDirectory));
    return ret;
  }
  /**
   * Set whether form attributes will be added to the request parameters
   *
   * @param mergeFormAttributes  true if they should be merged
   * @return reference to this for fluency
   */
  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
    def ret= BodyHandler.FACTORY.apply(this.delegate.setMergeFormAttributes(mergeFormAttributes));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.BodyHandler, BodyHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.BodyHandler arg -> new BodyHandler(arg);
  };
}
