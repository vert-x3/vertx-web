package io.vertx.ext.web.client.fullintegration.http.all;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class AllHtmlRequestHttpRoute extends AbstractHttpRoute
{
	public AllHtmlRequestHttpRoute(final Vertx vertx, final Router router)
	{
		super(vertx, router);
	}

	@Override
	public void processRoute(final Router router)
	{
		router.route().produces("text/html").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event)
			{
				event.response().setChunked(true);
				event.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
				event.response().putHeader(HttpHeaders.CONTENT_LANGUAGE, "fr, en");
				event.next();
			}
		});
	}
}
