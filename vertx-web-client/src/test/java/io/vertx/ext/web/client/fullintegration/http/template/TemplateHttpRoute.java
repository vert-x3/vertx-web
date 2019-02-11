package io.vertx.ext.web.client.fullintegration.http.template;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;

public class TemplateHttpRoute extends AbstractHttpRoute
{
	private final boolean production;
	
	public TemplateHttpRoute(final Vertx vertx, final Router router, final boolean production)
	{
		super(vertx, router);
		this.production = production;
	}

	@Override
	public void processRoute(final Router router)
	{
		if (!production)
		{
			// In this case, the mvel template will be recompiled with each request
			System.setProperty("io.vertx.ext.web.TemplateEngine.disableCache", "true");
		}
		
		// Call to the mvel rendering
		final TemplateEngine engine = MVELTemplateEngine.create(vertx);
		final TemplateHandler handler = TemplateHandler.create(engine, "dynamic", "text/html");
		router.routeWithRegex("/.*html").handler(handler);
	}
}
