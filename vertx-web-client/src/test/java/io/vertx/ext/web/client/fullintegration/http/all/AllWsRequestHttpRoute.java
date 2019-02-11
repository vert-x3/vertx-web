package io.vertx.ext.web.client.fullintegration.http.all;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class AllWsRequestHttpRoute extends AbstractHttpRoute
{
	public AllWsRequestHttpRoute(final Vertx vertx, final Router router)
	{
		super(vertx, router);
	}

	@Override
	public void processRoute(final Router router)
	{
		router.route("/ws/*").produces("application/json").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event)
			{
				event.response().setChunked(true);
				event.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
				event.next();
			}
		});
	}
}
