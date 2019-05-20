package io.vertx.ext.web.templ.handlebars.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.impl.TemplateHandlerImpl;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author romanovi
 * @since 4/8/17.
 */
public class TemplateHandlerImplTest {

  @Test
  public void testDefaultIndex() {
    TemplateEngine templateEngine = mock(TemplateEngine.class);
    RoutingContext routingContext = mock(RoutingContext.class);
    when(routingContext.normalisedPath()).thenReturn("/");
    Route currentRoute = mock(Route.class);
    when(currentRoute.getPath()).thenReturn("/");
    when(routingContext.currentRoute()).thenReturn(currentRoute);

    TemplateHandler templateHandler = new TemplateHandlerImpl(templateEngine, "templates", "ext");
    templateHandler.handle(routingContext);

    verify(templateEngine).render(any(JsonObject.class), eq("templates/index"), any());
  }

  @Test
  public void testSetIndex() {
    TemplateEngine templateEngine = mock(TemplateEngine.class);
    RoutingContext routingContext = mock(RoutingContext.class);
    when(routingContext.normalisedPath()).thenReturn("/");
    Route currentRoute = mock(Route.class);
    when(currentRoute.getPath()).thenReturn("/");
    when(routingContext.currentRoute()).thenReturn(currentRoute);

    TemplateHandler templateHandler = new TemplateHandlerImpl(templateEngine, "templates", "ext");
    templateHandler.setIndexTemplate("home");
    templateHandler.handle(routingContext);

    verify(templateEngine).render(any(JsonObject.class), eq("templates/home"), any());
  }

  @Test
  public void testTurnOffIndex() {
    TemplateEngine templateEngine = mock(TemplateEngine.class);
    RoutingContext routingContext = mock(RoutingContext.class);
    when(routingContext.normalisedPath()).thenReturn("/");
    Route currentRoute = mock(Route.class);
    when(currentRoute.getPath()).thenReturn("/");
    when(routingContext.currentRoute()).thenReturn(currentRoute);

    TemplateHandler templateHandler = new TemplateHandlerImpl(templateEngine, "templates", "ext");
    templateHandler.setIndexTemplate(null);
    templateHandler.handle(routingContext);

    verify(templateEngine).render(any(JsonObject.class), eq("templates/"), any());
  }

  @Test
  public void testSimpleTemplate() {
    TemplateEngine templateEngine = mock(TemplateEngine.class);
    RoutingContext routingContext = mock(RoutingContext.class);
    when(routingContext.normalisedPath()).thenReturn("/about");
    Route currentRoute = mock(Route.class);
    when(currentRoute.getPath()).thenReturn("/");
    when(routingContext.currentRoute()).thenReturn(currentRoute);

    TemplateHandler templateHandler = new TemplateHandlerImpl(templateEngine, "templates", "ext");
    templateHandler.handle(routingContext);

    verify(templateEngine).render(any(JsonObject.class), eq("templates/about"), any());
  }

}
