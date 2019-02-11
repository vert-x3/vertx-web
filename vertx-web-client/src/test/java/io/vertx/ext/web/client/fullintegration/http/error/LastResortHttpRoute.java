package io.vertx.ext.web.client.fullintegration.http.error;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class LastResortHttpRoute extends AbstractHttpRoute
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LastResortHttpRoute.class);
	
	public LastResortHttpRoute(final Vertx vertx, final Router router)
	{
		super(vertx, router);
	}

	@Override
	public void processRoute(final Router router)
	{
		LOGGER.debug("last chance to process the request, we redirect to /erreur404.html");

		// oups, houston, we have a problem !
		router.route().handler(ctx -> {
			ctx.reroute("/error404.html");
		});
	}
}
