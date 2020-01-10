package io.vertx.ext.web.handler.sse.impl;

import io.netty.buffer.Unpooled;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.SSEConnection;
import io.vertx.ext.web.handler.sse.SSEHandler;

import java.util.ArrayList;
import java.util.List;

public class SSEHandlerImpl implements SSEHandler {

	// README: DO NOT MUTATE THIS! (using EMPTY_BUFFER.appendBuffer(...) for instance)
	private static final Buffer EMPTY_BUFFER = Buffer.buffer(Unpooled.EMPTY_BUFFER);

	private final List<Handler<SSEConnection>> connectHandlers;
	private final List<Handler<SSEConnection>> closeHandlers;

	public SSEHandlerImpl() {
		connectHandlers = new ArrayList<>();
		closeHandlers = new ArrayList<>();
	}

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response();
		response.setChunked(true);
		SSEConnection connection = SSEConnection.create(context);
		String accept = request.getHeader("Accept");
		if (accept != null && !accept.contains("text/event-stream")) {
			connection.reject(406, "Not acceptable");
			return;
		}
		response.closeHandler(aVoid -> {
			closeHandlers.forEach(closeHandler -> closeHandler.handle(connection));
			connection.close();
		});
		response.headers().add("Content-Type", "text/event-stream");
		response.headers().add("Cache-Control", "no-cache");
		response.headers().add("Connection", "keep-alive");
		connectHandlers.forEach(handler -> handler.handle(connection));
		if (!connection.rejected()) {
			response.setStatusCode(200);
			response.setChunked(true);
			response.write(EMPTY_BUFFER);
		}
	}

	@Override
	public SSEHandler connectHandler(Handler<SSEConnection> handler) {
		connectHandlers.add(handler);
		return this;
	}

	@Override
	public SSEHandler closeHandler(Handler<SSEConnection> handler) {
		closeHandlers.add(handler);
		return this;
	}
}
