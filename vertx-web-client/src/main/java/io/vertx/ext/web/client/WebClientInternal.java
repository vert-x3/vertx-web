package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.impl.HttpContext;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface WebClientInternal extends WebClient {

  @GenIgnore
  void addInterceptor(Handler<HttpContext<?>> interceptor);

}
