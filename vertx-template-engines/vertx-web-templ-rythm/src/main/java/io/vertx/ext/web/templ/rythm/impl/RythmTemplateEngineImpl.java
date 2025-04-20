package io.vertx.ext.web.templ.rythm.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.CachedTemplate;
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
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    try {
      String src = adjustLocation(templateFile);
      CachedTemplate<String> template = getTemplate(src);

      if (template == null) {
        // either it's not cache or cache is disabled
        synchronized (this) {
          template = new CachedTemplate<>(fileSystem.readFileBlocking(src).toString());
        }
        putTemplate(src, template);
      }

      // respect the locale is present
      if (context.containsKey("lang")) {
        engine.prepare(Locale.forLanguageTag((String) context.get("lang")));
      } else {
        engine.prepare(Locale.getDefault());
      }

      return Future.succeededFuture(Buffer.buffer(engine.renderString(template.template(), context)));
    } catch(Exception ex) {
      return Future.failedFuture(ex);
    }
  }

  public RythmEngine unwrap() throws ClassCastException {
    return engine;
  }
}
