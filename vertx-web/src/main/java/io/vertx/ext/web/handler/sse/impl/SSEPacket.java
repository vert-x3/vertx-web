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

package io.vertx.ext.web.handler.sse.impl;

import io.vertx.core.buffer.Buffer;

class SSEPacket {

  private static final String END_OF_PACKET = "\n\n";
  private static final String LINE_SEPARATOR = "\n";
  private static final String FIELD_SEPARATOR = ":";

  private final StringBuilder payload;
  String headerName;
  String headerValue;

  SSEPacket() {
    payload = new StringBuilder();
  }

  boolean append(Buffer buffer) {
    String response = buffer.toString();
    boolean willTerminate = response.endsWith(END_OF_PACKET);
    String[] lines = response.split(LINE_SEPARATOR);
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      int idx = line.indexOf(FIELD_SEPARATOR);
      if (idx == -1) {
        continue; // ignore line
      }
      final String type = line.substring(0, idx);
      final String data = line.substring(idx + 2);
      if (i == 0 && headerName == null && !"data".equals(type)) {
        headerName = type;
        headerValue = data;
      } else {
        payload.append(data).append(LINE_SEPARATOR);
      }
    }
    return willTerminate;
  }

  @Override
  public String toString() {
    return payload.toString();
  }
}
