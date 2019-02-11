package io.vertx.ext.web.client.fullintegration.http;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public abstract class AbstractHttpRoute
{
	protected final Vertx vertx;

	private final Router router;

	public AbstractHttpRoute(Vertx vertx, Router router)
	{
		super();
		this.vertx = vertx;
		this.router = router;
	}

	public Vertx getVertx()
	{
		return vertx;
	}

	public abstract void processRoute(final Router router);

	protected void doRedirect(final HttpServerResponse response, final String url)
	{
		response.putHeader("location", url)
			.setStatusCode(302)
			.end();
	}
}
