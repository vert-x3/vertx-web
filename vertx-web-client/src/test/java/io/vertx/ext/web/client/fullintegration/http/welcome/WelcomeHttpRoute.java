package io.vertx.ext.web.client.fullintegration.http.welcome;

import java.time.Duration;
import java.time.Instant;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.fullintegration.http.AbstractHttpRoute;

public class WelcomeHttpRoute extends AbstractHttpRoute
{
	private final Instant serverStartDate;
	
	public WelcomeHttpRoute(final Vertx vertx, final Router router, final Instant serverStartDate)
	{
		super(vertx, router);
		this.serverStartDate = serverStartDate;
	}

	@Override
	public void processRoute(final Router router)
	{
		addDefaultRoute(router);
		addRouteGetIndex(router);
		addRouteGetPage3(router);
	}

	private void addDefaultRoute(final Router router)
	{
		// the welcome page is index.html
		router.route(HttpMethod.GET, "/").handler(new Handler<RoutingContext>()
		{
			@Override
			public void handle(RoutingContext event) 
			{
				// event.reroute("/index.html");
				doRedirect(event.response(), "/index.html");
			}}
		);
	}
	
	private void addRouteGetIndex(final Router router)
	{
		router.route(HttpMethod.GET, "/index.html").handler(new Handler<RoutingContext>(){
			@Override
			public void handle(RoutingContext event) 
			{
				// en-tete
				event.put("head.title", "vertx-web : welcome");
							
				final Duration duration = Duration.between(serverStartDate, Instant.now());
				event.put("upTime", "Starting from " + getUptime(duration));
				
				event.next();
			}}
		);
	}
	
	private void addRouteGetPage3(final Router router)
	{
		router.route(HttpMethod.GET, "/page3.html").handler(new Handler<RoutingContext>(){
			@Override
			public void handle(RoutingContext event) 
			{
				System.err.println("IllegalStateException :: houston, we have a problem");
				event.fail(504, new IllegalStateException("houston, we have a problem"));
			}
		});
	}
	
    private String getUptime(final Duration duration)
    {
    	final long days = duration.toDays();
    	final long hours = duration.toHours() % 24;
    	final long minutes = duration.toMinutes() % 60;
    	final long seconds = duration.getSeconds() % 60;
        final StringBuilder sb = new StringBuilder(64);
        
        if (days > 0)
        {
        	sb.append(days);
        	sb.append(" day");
        	
        	if (days > 1)
        	{
        		sb.append("s");
        	}
        	sb.append(" ");
        }
        
        if(hours > 0)
        {
        	sb.append(hours);
        	sb.append(" hour");
        	
        	if (hours > 1)
        	{
        		sb.append("s");
        	}
        	sb.append(" et ");
        }
        
        if (days == 0 && hours == 0 && minutes == 0)
        {
        	sb.append(seconds);
        	sb.append(" second");
        	
        	if (seconds > 1)
        	{
        		sb.append("s");
        	}
        }
        else
        {
        	sb.append(minutes);
        	sb.append(" minute");
        	
        	if (minutes > 1)
        	{
        		sb.append("s");
        	}
        }
        
        return(sb.toString());
    }
}
