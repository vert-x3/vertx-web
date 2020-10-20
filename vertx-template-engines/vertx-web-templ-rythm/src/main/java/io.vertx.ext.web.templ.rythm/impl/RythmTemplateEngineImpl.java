package io.vertx.ext.web.templ.rythm.impl;

import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.rythm.RythmTemplateEngine;
import org.rythmengine.RythmEngine;

import java.util.Locale;
import java.util.Map;

/**
 * @author Konstantin Volivach kostya05983@mail.ru
 */
public class RythmTemplateEngineImpl extends CachingTemplateEngine<String>  implements RythmTemplateEngine {

  private final FileSystem fileSystem;
  private final RythmEngine engine;

  public RythmTemplateEngineImpl(Vertx vertx, String extension) {
    super(vertx, extension);
    this.fileSystem = vertx.fileSystem();
    engine = new RythmEngine();
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String src = adjustLocation(templateFile);
      TemplateHolder<String> template = getTemplate(src);

      if (template == null) {
        // either it's not cache or cache is disabled
        synchronized (this) {
          template = new TemplateHolder<>(fileSystem.readFileBlocking(src).toString());
        }
        putTemplate(src, template);
      }

      // respect the locale is present
      if (context.containsKey("lang")) {
        engine.prepare(Locale.forLanguageTag((String) context.get("lang")));
      } else {
        engine.prepare(Locale.getDefault());
      }

      handler.handle(Future.succeededFuture(Buffer.buffer(engine.renderString(template.template(), context))));
    } catch(Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  public <T> T unwrap() throws ClassCastException {
    return (T) engine;
  }
}
