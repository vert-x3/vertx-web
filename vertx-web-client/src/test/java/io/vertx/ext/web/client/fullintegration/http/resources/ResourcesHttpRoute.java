package io.vertx.ext.web.client.fullintegration.http.resources;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;
import io.vertx.ext.web.handler.StaticHandler;

public class ResourcesHttpRoute extends AbstractHttpRoute
{
	private final boolean production;

	public ResourcesHttpRoute(final Vertx vertx, final Router router, boolean production)
	{
		super(vertx, router);
		this.production = production;
	}

	@Override
	public void processRoute(final Router router)
	{
		router.routeWithRegex("\\/images\\/.*\\.png").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event)
			{
				event.response().setChunked(true);
				event.response().putHeader(HttpHeaders.CONTENT_TYPE, "image/png;charset=UTF-8");
				event.response().putHeader(HttpHeaders.CONTENT_ENCODING, "binary");
				event.next();
			}
		});

		router.route("/images/*").handler(StaticHandler.create("static/images").setDefaultContentEncoding("UTF-8").setCachingEnabled(production));
	}
}
