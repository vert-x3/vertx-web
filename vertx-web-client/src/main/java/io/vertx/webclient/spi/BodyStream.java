package io.vertx.webclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface BodyStream<T> extends WriteStream<Buffer>, Handler<Throwable> {

  Future<T> state();

}
