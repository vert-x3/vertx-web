package io.vertx.ext.web.handler.sse;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.handler.sse.impl.EventSourceImpl;

public interface EventSource {

	static EventSource create(Vertx vertx, HttpClientOptions options) {
		return new EventSourceImpl(vertx, options);
	}

	@Fluent
  EventSource connect(String path, Handler<AsyncResult<Void>> handler);

	@Fluent
	default EventSource close() {
		return null;
	}

	@Fluent
  EventSource connect(String path, String lastEventId, Handler<AsyncResult<Void>> handler);

	@Fluent
  EventSource onMessage(Handler<String> messageHandler);

	@Fluent
  EventSource onEvent(String eventName, Handler<String> handler);

	@Fluent
	default EventSource onClose(Handler<Void> handler) {
		return null;
	}

	String lastId();
}
