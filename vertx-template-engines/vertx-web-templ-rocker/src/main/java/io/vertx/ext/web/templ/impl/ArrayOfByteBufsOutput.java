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

package io.vertx.ext.web.templ.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.runtime.AbstractRockerOutput;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class ArrayOfByteBufsOutput extends AbstractRockerOutput<ArrayOfByteBufsOutput> {

  private final List<ByteBuf> arrays;

  public ArrayOfByteBufsOutput(ContentType contentType, String charsetName) {
    super(contentType, charsetName, 0);
    this.arrays = new ArrayList<ByteBuf>();
  }

  public ArrayOfByteBufsOutput(ContentType contentType, Charset charset) {
    super(contentType, charset, 0);
    this.arrays = new ArrayList<ByteBuf>();
  }

  public List<ByteBuf> getArrays() {
    return arrays;
  }

  @Override
  public ArrayOfByteBufsOutput w(String string) throws IOException {
    if (string.length() != 0) {
      byte[] bytes = string.getBytes(charset);
      arrays.add(Unpooled.wrappedBuffer(bytes));
      this.byteLength += bytes.length;
    }
    return this;
  }

  @Override
  public ArrayOfByteBufsOutput w(byte[] bytes) throws IOException {
    if (bytes.length != 0) {
      arrays.add(Unpooled.wrappedBuffer(bytes));
      this.byteLength += bytes.length;
    }
    return this;
  }

  public ByteBuf toByteBuf() {
    final int arraysSize = arrays.size();
    switch (arraysSize) {
    case 0:
      return Unpooled.EMPTY_BUFFER;
    case 1:
      return arrays.get(0);
    default:
      return new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, arraysSize, arrays);
    }
  }

  public void release() {
    for (ByteBuf byteBuf : arrays) {
      byteBuf.release();
    }
  }
}
