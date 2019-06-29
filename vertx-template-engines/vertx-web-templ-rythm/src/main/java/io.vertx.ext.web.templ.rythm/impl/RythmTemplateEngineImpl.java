package io.vertx.ext.web.templ.rythm.impl;

import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.templ.rythm.RythmTemplateEngine;
import org.rythmengine.RythmEngine;

import java.io.File;
import java.util.Map;

/**
 * @author Konstantin Volivach kostya05983@mail.ru
 */
public class RythmTemplateEngineImpl implements RythmTemplateEngine {
  private final RythmEngine engine ;


  public RythmTemplateEngineImpl() {
    engine = new RythmEngine();
  }

  @Override
  public void render(Map<String, Object> context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    final Buffer buffer = Buffer.buffer();
    try {
      final String rendered = engine.render(new File(templateFileName), context);
      buffer.appendString(rendered);
      handler.handle(Future.succeededFuture(buffer));
    } catch(Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }
}
