/*
 * Copyright 2020 Red Hat, Inc.
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

package io.vertx.ext.web.handler.sse;

/**
 * This is a final class to match io.vertx.core.HttpHeaders
 * Since maybe enums can cause trouble with codegen ? idk
 */
public final class SSEHeaders {

  private SSEHeaders() {
  }

  public static final String EVENT = "event";
  public static final String ID = "id";
  public static final String RETRY = "retry";
  public static final String LAST_EVENT_ID = "Last-Event-ID";

}
