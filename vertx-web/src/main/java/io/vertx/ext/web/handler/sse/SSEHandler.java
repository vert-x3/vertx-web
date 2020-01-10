package io.vertx.ext.web.handler.sse;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.impl.SSEHandlerImpl;

@VertxGen
public interface SSEHandler extends Handler<RoutingContext> {

	static SSEHandler create() {
		return new SSEHandlerImpl();
	}

	@Fluent
	public SSEHandler connectHandler(Handler<SSEConnection> connection);

	@Fluent
	public SSEHandler closeHandler(Handler<SSEConnection> connection);
}
