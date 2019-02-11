package io.vertx.ext.web.client.fullintegration.http.all;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class AllRequestHttpRoute extends AbstractHttpRoute
{
	private final String serverHost;
	
	private final String cspForAllPages;

	public AllRequestHttpRoute(final Vertx vertx, final Router router, final String serverHost)
	{
		super(vertx, router);
		this.serverHost = serverHost;
		this.cspForAllPages = generateCsp(serverHost);
	}

	@Override
	public void processRoute(final Router router)
	{
		router.route().handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event)
			{
				final HttpServerResponse response = event.response();
				response.setChunked(true);
				response.putHeader("Content-Security-Policy", cspForAllPages);
				response.putHeader("X-Frame-Options", "SAMEORIGIN");
				response.putHeader("X-XSS-Protection", "1;mode=block");
				response.putHeader("X-Content-Type-Options", "nosniff");

				event.next();
			}
		});
	}
	
	
	private String generateCsp(final String serverHost) 
	{
		return "base-uri 'self';"
			+ "default-src http://" + serverHost + "/;"
			+ "block-all-mixed-content;"
			+ "media-src 'self';"
			+ "object-src 'none';"
			+ "style-src 'self' 'unsafe-inline' 'unsafe-eval';"
			+ "script-src http://" + serverHost + "/ 'unsafe-inline' 'unsafe-eval';"
			+ "connect-src wss://" + serverHost + "/ http://" + serverHost + "/ ;"
			+ "worker-src 'self';";
	}
}
