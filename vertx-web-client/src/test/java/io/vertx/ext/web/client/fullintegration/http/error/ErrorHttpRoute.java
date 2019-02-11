package io.vertx.ext.web.client.fullintegration.http.error;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class ErrorHttpRoute extends AbstractHttpRoute
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHttpRoute.class);
	
	public ErrorHttpRoute(final Vertx vertx, final Router router)
	{
		super(vertx, router);
	}

	@Override
	public void processRoute(final Router router)
	{
		addRouteIfError(router);
		addRouteIf404Error(router);
		defineRoutingWhenErrorOccured(router);
	}

	private void defineRoutingWhenErrorOccured(Router router)
	{
		router.route().failureHandler(processError());
	}
	
	private Handler<RoutingContext> processError()
	{
		return new Handler<RoutingContext>() 
		{
			@Override
			public void handle(final RoutingContext failEvent) 
			{
				LOGGER.warn("Error occured with the folloging url requested: " + failEvent.request().absoluteURI(), failEvent);
								
				if (failEvent.statusCode() == 404 || failEvent.statusCode() < 0) 
				{
					failEvent.reroute(HttpMethod.GET, "/error404.html");
				}
				else
				{
					failEvent.put("statusCode", failEvent.statusCode());
					failEvent.reroute(HttpMethod.GET, "/error.html");
				}
			}
		};
	}
	

	private void addRouteIfError(Router router)
	{
		router.route(HttpMethod.GET, "/error.html").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event) 
			{
				event.put("head.title", "vertx-web : unexpected error 500");
				event.next();
			}
		});
	}

	private void addRouteIf404Error(Router router)
	{
		router.route(HttpMethod.GET, "/error404.html").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event)
			{
				event.put("head.title", "vertx-web : resource not found 404");
				event.next();
			}}
		);
	}
}
