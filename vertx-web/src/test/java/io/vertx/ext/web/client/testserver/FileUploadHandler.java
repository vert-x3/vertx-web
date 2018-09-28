package io.vertx.ext.web.client.testserver;

import java.util.Set;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public final class FileUploadHandler implements Handler<RoutingContext> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadHandler.class);

	@Override
	public void handle(RoutingContext context) {
		LOGGER.debug("Post called!");
		Set<FileUpload> uploads = context.fileUploads();
		LOGGER.debug("Number of uploads: " + uploads.size());
		HttpServerResponse response = context.response();
		if (uploads.size() > 0) {
			response.setStatusCode(201);
		} else {
			response.setStatusCode(400);
		}
		response.end();
	}

}
