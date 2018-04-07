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
package io.vertx.ext.web.client.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.multipart.FormDataPart;
import io.vertx.ext.web.multipart.MultipartForm;

import java.io.File;

/**
 * A stream that sends a multipart form.
 */
public class MultipartFormUpload implements ReadStream<Buffer> {

  private static final UnpooledByteBufAllocator ALLOC = new UnpooledByteBufAllocator(false);

  private final Context context;
  private DefaultFullHttpRequest request;
  private HttpPostRequestEncoder encoder;
  private Handler<Throwable> exceptionHandler;
  private Handler<Buffer> dataHandler;
  private Handler<Void> endHandler;
  private boolean paused;
  private boolean sentCheck;

  public MultipartFormUpload(Context context, MultipartForm parts, boolean multipart) throws Exception {
    this.context = context;
    this.request = new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1,
      io.netty.handler.codec.http.HttpMethod.POST,
      "/");
    this.encoder = new HttpPostRequestEncoder(request, multipart);
    for (FormDataPart formDataPart : parts) {
      if (formDataPart.isAttribute()) {
        encoder.addBodyAttribute(formDataPart.name(), formDataPart.value());
      } else {
        encoder.addBodyFileUpload(formDataPart.name(),
          formDataPart.filename(), new File(formDataPart.pathname()),
          formDataPart.mediaType(), formDataPart.isText());
      }
    }
    encoder.finalizeRequest();
    this.paused = true;
  }

  public MultiMap headers() {
    return new HeadersAdaptor(request.headers());
  }

  private synchronized void checkNextTick() {
    if (!paused && encoder != null && !sentCheck) {
      sentCheck = true;
      context.runOnContext(v -> {
        Handler<Void> endHandler;
        Handler<Buffer> dataHandler;
        Handler<Throwable> exceptionHandler;
        synchronized (MultipartFormUpload.this) {
          sentCheck = false;
          endHandler = this.endHandler;
          dataHandler = this.dataHandler;
          exceptionHandler = this.exceptionHandler;
        }
        if (encoder.isChunked()) {
          try {
            HttpContent chunk = encoder.readChunk(ALLOC);
            ByteBuf content = chunk.content();
            Buffer buff = Buffer.buffer(content);
            if (dataHandler != null) {
              dataHandler.handle(buff);
            }
            if (encoder.isEndOfInput()) {
              request = null;
              encoder = null;
              if (endHandler != null) {
                endHandler.handle(null);
              }
            } else {
              checkNextTick();
            }
          } catch (Exception e) {
            request = null;
            encoder = null;
            if (exceptionHandler != null) {
              exceptionHandler.handle(e);
            }
          }
        } else {
          ByteBuf content = request.content();
          Buffer buffer = Buffer.buffer(content);
          if (dataHandler != null) {
            dataHandler.handle(buffer);
          }
          if (endHandler != null) {
            endHandler.handle(null);
          }
          request = null;
          encoder = null;
        }
      });
    }
  }

  @Override
  public synchronized MultipartFormUpload exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public synchronized MultipartFormUpload handler(Handler<Buffer> handler) {
    if (dataHandler == null) {
      dataHandler = handler;
      if (paused) {
        resume();
      }
    } else {
      dataHandler = handler;
    }
    return this;
  }

  @Override
  public synchronized MultipartFormUpload pause() {
    paused = true;
    return this;
  }

  @Override
  public synchronized MultipartFormUpload resume() {
    if (paused) {
      paused = false;
      checkNextTick();
    }
    return this;
  }

  @Override
  public synchronized MultipartFormUpload endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}
