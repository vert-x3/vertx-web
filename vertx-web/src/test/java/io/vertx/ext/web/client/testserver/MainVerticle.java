package io.vertx.ext.web.client.testserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router mainRouter = Router.router(vertx);
		Router v3ApiRouter = Router.router(vertx);

		mainRouter.route().last().handler(new DefaultHandler());
		mainRouter.mountSubRouter("/api/v1", v3ApiRouter);

		v3ApiRouter.post("/post").handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(false)).handler(new FileUploadHandler());
		v3ApiRouter.route().last().handler(new DefaultHandler());

		vertx.createHttpServer().requestHandler(v3ApiRouter::accept).listen(config().getInteger("http.port",8080), serverStartup -> completeStartup(serverStartup, startFuture));
	}

	private void completeStartup(AsyncResult<HttpServer> serverStartup, Future<Void> future) {
		if (serverStartup.succeeded()) {
			future.complete();
		} else {
			future.fail(serverStartup.cause());
		}
	}

}
