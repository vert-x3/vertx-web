package io.vertx.ext.web.impl;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jansorg
 */
public class PrefixedRouterImplTest extends WebTestBase {
  @Test
  public void test_prefixed_path() {
    Router r = new PrefixedRouterImpl(vertx, "/api/:segment", false);

    Assert.assertEquals("/api/:segment", r.route().getPath());
    Assert.assertEquals("/api/:segment/ping", r.route("/ping").getPath());
    Assert.assertEquals("/api/:segmentping", r.route("ping").getPath());
    Assert.assertEquals("/api/:segment/*", r.route("/*").getPath());

    Assert.assertEquals("/api/:segment/user/login", r.route("/user/login").getPath());

    Route routeWithRegex = r.routeWithRegex("/user/login");
    Assert.assertNull(routeWithRegex.getPath());
    Assert.assertTrue(routeWithRegex.toString().contains("pattern:\\Q/api/:segment\\E/user/login"));
  }

  @Test
  public void test_prefixed_regex_path() {
    Router r = new PrefixedRouterImpl(vertx, "/api/[0-9]+", true);

    Assert.assertNull(r.route().getPath());
    Assert.assertTrue(r.route().toString().contains("pattern:/api/[0-9]+"));
    Assert.assertTrue(r.route("/ping").toString(), (r.route("/ping").toString().contains("pattern:/api/[0-9]+\\Q/ping\\E")));
    Assert.assertTrue(r.route("/*").toString().contains("pattern:/api/[0-9]+\\Q/*\\E"));

    Assert.assertTrue(r.routeWithRegex("/*").toString().contains("pattern:/api/[0-9]+/*"));
  }

  @Test
  public void test_get() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.get().getPath());
    Assert.assertEquals("/api/login", r.get("/login").getPath());
    Assert.assertEquals("/apilogin", r.get("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.getWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_post() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.post().getPath());
    Assert.assertEquals("/api/login", r.post("/login").getPath());
    Assert.assertEquals("/apilogin", r.post("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.postWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_put() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.put().getPath());
    Assert.assertEquals("/api/login", r.put("/login").getPath());
    Assert.assertEquals("/apilogin", r.put("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.putWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_connect() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.connect().getPath());
    Assert.assertEquals("/api/login", r.connect("/login").getPath());
    Assert.assertEquals("/apilogin", r.connect("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.connectWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_options() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.options().getPath());
    Assert.assertEquals("/api/login", r.options("/login").getPath());
    Assert.assertEquals("/apilogin", r.options("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.optionsWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }


  @Test
  public void test_delete() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.delete().getPath());
    Assert.assertEquals("/api/login", r.delete("/login").getPath());
    Assert.assertEquals("/apilogin", r.delete("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.deleteWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_head() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.head().getPath());
    Assert.assertEquals("/api/login", r.head("/login").getPath());
    Assert.assertEquals("/apilogin", r.head("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.headWithRegex("/[a-z]+[0-9]+").toString(), r.headWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_patch() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.patch().getPath());
    Assert.assertEquals("/api/login", r.patch("/login").getPath());
    Assert.assertEquals("/apilogin", r.patch("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.patchWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }

  @Test
  public void test_trace() {
    Router r = new PrefixedRouterImpl(vertx, "/api", false);
    Assert.assertEquals("/api", r.trace().getPath());
    Assert.assertEquals("/api/login", r.trace("/login").getPath());
    Assert.assertEquals("/apilogin", r.trace("login").getPath());

    //there's getter for pattern
    Assert.assertTrue(r.traceWithRegex("/[a-z]+[0-9]+").toString().contains("pattern:\\Q/api\\E/[a-z]+[0-9]+"));
  }
}
