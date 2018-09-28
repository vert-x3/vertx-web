package io.vertx.ext.web.client.testserver;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public final class DefaultHandler implements Handler<RoutingContext> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHandler.class);

	@Override
	public void handle(RoutingContext context) {
		LOGGER.info("Handling default call");
		// This handler will be called for every request
		HttpServerResponse response = context.response();
		response.putHeader("content-type", "text/plain");

		// Write to the response and end it
		response.end("Vertx Server to Test Vertx Client.");
	}

}
