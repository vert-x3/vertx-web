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

import java.util.List;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.RockerTemplateEngine;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class RockerTemplateEngineImpl extends CachingTemplateEngine<Void> implements RockerTemplateEngine {

  public RockerTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
  }

  @Override
  public RockerTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public RockerTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateDirectory, String templateFileName,
      Handler<AsyncResult<Buffer>> handler) {
    try {
      templateFileName = templateDirectory + templateFileName;
      String templatePath = adjustLocation(templateFileName);

      BindableRockerModel model = Rocker.template(templatePath);
      model.bind("context", context);
      model.bind(context.data());

      ArrayOfByteArraysOutput output = model.render(ArrayOfByteArraysOutput.FACTORY);

      List<byte[]> listOfByteArrays = output.getArrays();
      int listOfByteArraysSize = listOfByteArrays.size();
      byte[][] arrayOfByteArrays = listOfByteArrays.toArray(new byte[listOfByteArraysSize][]);
      ByteBuf byteBuf = Unpooled.wrappedBuffer(listOfByteArraysSize, arrayOfByteArrays);

      handler.handle(Future.succeededFuture(Buffer.buffer(byteBuf)));
    } catch (final Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
