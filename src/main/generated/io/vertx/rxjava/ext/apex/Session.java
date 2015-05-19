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

package io.vertx.rxjava.ext.apex;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;

/**
 * Represents a browser session.
 * <p>
 * Sessions persist between HTTP requests for a single browser session. They are deleted when the browser is closed, or
 * they time-out. Session cookies are used to maintain sessions using a secure UUID.
 * <p>
 * Sessions can be used to maintain data for a browser session, e.g. a shopping basket.
 * <p>
 * The context must have first been routed to a {@link  io.vertx.rxjava.ext.apex.handler.SessionHandler}
 * for sessions to be available.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.apex.Session original} non RX-ified interface using Vert.x codegen.
 */

public class Session {

  final io.vertx.ext.apex.Session delegate;

  public Session(io.vertx.ext.apex.Session delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * @return The unique ID of the session. This is generated using a random secure UUID.
   * @return 
   */
  public String id() { 
    String ret = this.delegate.id();
    return ret;
  }

  /**
   * Put some data in a session
   * @param key the key for the data
   * @param obj the data
   * @return a reference to this, so the API can be used fluently
   */
  public Session put(String key, Object obj) { 
    this.delegate.put(key, obj);
    return this;
  }

  /**
   * Get some data from the session
   * @param key the key of the data
   * @return the data
   */
  public <T> T get(String key) { 
    T ret = (T) this.delegate.get(key);
    return ret;
  }

  /**
   * Remove some data from the session
   * @param key the key of the data
   * @return the data that was there or null if none there
   */
  public <T> T remove(String key) { 
    T ret = (T) this.delegate.remove(key);
    return ret;
  }

  /**
   * @return the time the session was last accessed
   * @return 
   */
  public long lastAccessed() { 
    long ret = this.delegate.lastAccessed();
    return ret;
  }

  /**
   * Destroy the session
   */
  public void destroy() { 
    this.delegate.destroy();
  }

  /**
   * @return has the session been destroyed?
   * @return 
   */
  public boolean isDestroyed() { 
    boolean ret = this.delegate.isDestroyed();
    return ret;
  }

  /**
   * @return the amount of time in ms, after which the session will expire, if not accessed.
   * @return 
   */
  public long timeout() { 
    long ret = this.delegate.timeout();
    return ret;
  }

  /**
   * Mark the session as being accessed.
   */
  public void setAccessed() { 
    this.delegate.setAccessed();
  }


  public static Session newInstance(io.vertx.ext.apex.Session arg) {
    return new Session(arg);
  }
}
