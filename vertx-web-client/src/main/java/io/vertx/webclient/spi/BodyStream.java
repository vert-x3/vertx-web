package io.vertx.webclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * The body stream decodes a {@link WriteStream<Buffer>} into the a {@code T} instance.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface BodyStream<T> extends WriteStream<Buffer>, Handler<Throwable> {

  /**
   * @return the future signaling the completion of the stream
   */
  Future<T> result();

}
