package io.vertx.ext.web.client.fullintegration;

import java.time.Instant;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.all.AllHtmlRequestHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.all.AllRequestHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.all.AllWsRequestHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.error.ErrorHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.error.LastResortHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.resources.ResourcesHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.template.TemplateHttpRoute;
import io.vertx.ext.web.client.fullintegration.http.welcome.WelcomeHttpRoute;

public class FullIntegrationTest 
{
	public static final Vertx vertx = Vertx.vertx();
	
	private static HttpServer server;
	
	public static void main(String[] args) 
	{
		final Instant serverStartDate = Instant.now();
		
		System.out.println("FullIntegrationTest initializing ...");
		
		// Force to read the content files in UTF-8
		System.setProperty("file.encoding","UTF-8");
		server = vertx.createHttpServer();
		
		final boolean production = false;
		final Router router = Router.router(vertx);
		final String serverHost = "localhost";
		
		// global management of roads
		final AllRequestHttpRoute allRoute = new AllRequestHttpRoute(vertx, router, serverHost);
		final AllHtmlRequestHttpRoute allHtmlRoute = new AllHtmlRequestHttpRoute(vertx, router);
		final AllWsRequestHttpRoute allWsRoute = new AllWsRequestHttpRoute(vertx, router);
		
		// rendering in error case
		final ErrorHttpRoute errorRoute = new ErrorHttpRoute(vertx, router);
		final LastResortHttpRoute lastResortErrorRoute = new LastResortHttpRoute(vertx, router);

		// resources : images, javascript, css, etc.
		final ResourcesHttpRoute resourcesRoute = new ResourcesHttpRoute(vertx, router, production);

		// welcome page
		final WelcomeHttpRoute welcomeRoute = new WelcomeHttpRoute(vertx, router, serverStartDate);
		
		final TemplateHttpRoute templateRoute = new TemplateHttpRoute(vertx, router, production);
		
		processRoutes(router, 
			allRoute,
			
			allHtmlRoute,
			
			allWsRoute,
			
			errorRoute,
			
			resourcesRoute,
			
			welcomeRoute,
			
			templateRoute,
			
			lastResortErrorRoute
		);
			
		server.requestHandler(router);
		
		startHttpServer(new Handler<AsyncResult<Void>>() 
		{
			@Override
			public void handle(final AsyncResult<Void> startHttpServerEvent) 
			{
				if (startHttpServerEvent.succeeded())
				{
					System.out.println("Web server started on http://localhost/");
				}
				else
				{
					System.err.println("Fail to start the web server because : " + startHttpServerEvent.cause().getMessage());
					System.exit(1);
				}
			}
		});
	}
	
	private static void startHttpServer(final Handler<AsyncResult<Void>> startingHttpServerEvent) 
	{
		vertx.<HttpServer>executeBlocking(future -> {
			server.listen(80, new Handler<AsyncResult<HttpServer>>()
			{
				@Override
				public void handle(AsyncResult<HttpServer> event) 
				{
					if (event.succeeded())
					{
						future.complete(event.result());
					}
					else
					{
						future.fail(new RuntimeException("failed to start web server on http://localhost/", event.cause()));
					}
				}
			});
		}, res -> {
			if (res.succeeded())
			{
				System.out.println("Web server starting on http://localhost/ ...");
				startingHttpServerEvent.handle(Future.succeededFuture());
			}
			else
			{
				System.err.println("Fail to start the web server because :: " + res.cause().getMessage());
				startingHttpServerEvent.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	private static void processRoutes(final Router router, final AbstractHttpRoute... routes)
	{
		if (routes != null && routes.length > 0)
		{
			for(final AbstractHttpRoute route : routes)
			{
				route.processRoute(router);
			}
		}
	}
}
