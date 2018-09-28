package io.vertx.ext.web.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.impl.MultipartFormUpload;
import io.vertx.ext.web.client.testserver.MainVerticle;
import io.vertx.ext.web.multipart.MultipartForm;

@RunWith(VertxUnitRunner.class)
public class MultipartFileUploadTest {
	private Vertx vertx;
	private int port;
	private final static Logger LOGGER = LoggerFactory.getLogger(MultipartFileUploadTest.class);

	@Before
	public void deployVerticle(TestContext testContext) throws IOException {
		vertx = Vertx.vertx();

		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();

		DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

		vertx.deployVerticle(MainVerticle.class.getName(), options, testContext.asyncAssertSuccess());
	}

	@After
	public void stopVertx(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testPostFile(TestContext context) throws Exception {
		Async async = context.async();

		URL testFileResource = this.getClass().getResource("/test.bin");
		
		final MultipartForm parts = MultipartForm.create();
		parts.attribute("attribute", "value");
		parts.binaryFileUpload("fileToUpload","file.bin",testFileResource.getFile(), "application/octet-stream");

		HttpClientRequest request = vertx.createHttpClient().post(port, "localhost", "/post")
				.handler(response -> {
					context.assertEquals(201, response.statusCode());
				}).endHandler(end -> {
					LOGGER.info("Request complete.");
					async.complete();
				}).exceptionHandler(t -> {
					LOGGER.error(t);
					context.fail();
					async.complete();
				});

		MultipartFormUpload upload = new MultipartFormUpload(vertx.getOrCreateContext(), parts, true);
		upload.run();
		upload.endHandler(e -> {
			LOGGER.info("Finished handling data.");
			// LOGGER.info("" + request.headers());
			// request.end();
		});
		upload.handler(data -> {
			LOGGER.info("Handling data: " + data);
			request.putHeader("Content-Length", String.valueOf(data.length()));
			//request.putHeader("Content-Type", "multipart/form-data");

			request.write(data).end();
		});
		upload.exceptionHandler(t -> {
			LOGGER.error(t);
			context.fail();
			async.complete();
		});
		upload.resume();

		async.await(3000L);
	}
}
