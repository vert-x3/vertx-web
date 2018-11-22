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

package io.vertx.ext.web.templ.rocker.impl;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.runtime.AbstractRockerOutput;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class VertxBufferOutput extends AbstractRockerOutput<VertxBufferOutput> {

  public static VertxBufferOutputFactory FACTORY = new VertxBufferOutputFactory();

  private final Buffer buffer;

  public VertxBufferOutput(ContentType contentType, String charsetName) {
    super(contentType, charsetName, 0);
    this.buffer = Buffer.buffer();
  }

  public VertxBufferOutput(ContentType contentType, Charset charset) {
    super(contentType, charset, 0);
    this.buffer = Buffer.buffer();
  }

  public Buffer getBuffer() {
    return buffer;
  }

  @Override
  public VertxBufferOutput w(String string) throws IOException {
    buffer.appendBytes(string.getBytes(charset));
    return this;
  }

  @Override
  public VertxBufferOutput w(byte[] bytes) throws IOException {
    buffer.appendBytes(bytes);
    return this;
  }

}
