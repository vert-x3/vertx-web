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

package io.vertx.rxjava.ext.web.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.Handler;

/**
 * A handler which gathers the entire request body and sets it on the .
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.BodyHandler original} non RX-ified interface using Vert.x codegen.
 */

public class BodyHandler implements Handler<RoutingContext> {

  final io.vertx.ext.web.handler.BodyHandler delegate;

  public BodyHandler(io.vertx.ext.web.handler.BodyHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.web.RoutingContext) arg0.getDelegate());
  }

  /**
   * Create a body handler with defaults
   * @return the body handler
   */
  public static BodyHandler create() { 
    BodyHandler ret= BodyHandler.newInstance(io.vertx.ext.web.handler.BodyHandler.create());
    return ret;
  }

  /**
   * Set the maximum body size -1 means unlimited
   * @param bodyLimit the max size
   * @return reference to this for fluency
   */
  public BodyHandler setBodyLimit(long bodyLimit) { 
    this.delegate.setBodyLimit(bodyLimit);
    return this;
  }

  /**
   * Set the uploads directory to use
   * @param uploadsDirectory the uploads directory
   * @return reference to this for fluency
   */
  public BodyHandler setUploadsDirectory(String uploadsDirectory) { 
    this.delegate.setUploadsDirectory(uploadsDirectory);
    return this;
  }

  /**
   * Set whether form attributes will be added to the request parameters
   * @param mergeFormAttributes true if they should be merged
   * @return reference to this for fluency
   */
  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) { 
    this.delegate.setMergeFormAttributes(mergeFormAttributes);
    return this;
  }


  public static BodyHandler newInstance(io.vertx.ext.web.handler.BodyHandler arg) {
    return arg != null ? new BodyHandler(arg) : null;
  }
}
