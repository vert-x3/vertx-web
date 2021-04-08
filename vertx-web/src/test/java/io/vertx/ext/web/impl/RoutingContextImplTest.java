package io.vertx.ext.web.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;


public class RoutingContextImplTest extends WebTestBase {

  @AfterClass
  public static void oneTimeTearDown() throws IOException {
    cleanupFileUploadDir();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(BodyHandler.create());
  }

  @Test
  public void test_empty_array_as_json_array_yields_empty_json_array() throws Exception {
    router.route().handler(event -> {
      assertEquals(new JsonArray(), event.getBodyAsJsonArray());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("[]"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_empty_yields_null_json_types() throws Exception {
    router.route().handler(event -> {
      assertNull(event.getBodyAsJsonArray());
      assertNull(event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer(""));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_one_item_array_as_json_array_yields_one_item_json_array() throws Exception {
    router.route().handler(event -> {
      JsonArray array = new JsonArray(
        Collections.singletonList(
          new JsonObject(Collections.singletonMap("foo", "bar"))
        )
      );
      assertEquals(array, event.getBodyAsJsonArray());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("[ { \"foo\": \"bar\" } ]"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_null_literal_array_as_json_array_yields_null_json_array() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJsonArray());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("null"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_non_array_as_json_array_fails_json_array() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJsonArray());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("\"1234"));
    }, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(), null);
  }

  @Test
  public void test_invalid_array_as_json_array_fails_json_array() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJsonArray());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("1234"));
    }, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(), null);
  }

  @Test
  public void test_empty_object_as_json_yields_empty_json_object() throws Exception {
    router.route().handler(event -> {
      assertEquals(new JsonObject(), event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("{ }"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_object_as_json_yields_json_object() throws Exception {
    router.route().handler(event -> {
      assertEquals(new JsonObject(Collections.singletonMap("foo", "bar")), event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("{ \"foo\": \"bar\" }"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_null_literal_object_as_json_yields_empty_json_object() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("null"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_invalid_json_object_as_json_fails_json_object() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("\"1234"));
    }, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(), null);
  }

  @Test
  public void test_non_json_object_as_json_fails_json_object() throws Exception {
    router.route().handler(event -> {
      assertEquals(null, event.getBodyAsJson());
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("1234"));
    }, HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase(), null);
  }

  @Test
  public void test_remove_data() throws Exception {
    router.route().handler(event -> {
      String foo = event.getBodyAsJson().encode();
      event.put("foo", foo);
      String removedFoo = event.remove("foo");
      assertEquals(removedFoo, foo);
      assertNull(event.get("foo"));
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(Buffer.buffer("{ \"foo\": \"bar\" }"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testAttachment() throws Exception {
    router.route().handler(event -> {
      event.attachment("myfile.pdf").response().end("PDF");
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("attachment; filename=myfile.pdf", res.getHeader("Content-Disposition"));
      assertEquals("application/pdf", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testAttachmentWithoutMIME() throws Exception {
    router.route().handler(event -> {
      event.attachment("myfile.paulo").response().end("PDF");
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("attachment; filename=myfile.paulo", res.getHeader("Content-Disposition"));
      assertNull(res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testJsonObject() throws Exception {
    router.route().handler(ctx -> {
      ctx.json(new JsonObject());
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testJsonArray() throws Exception {
    router.route().handler(ctx -> {
      ctx.json(new JsonArray());
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testJsonPrimitive() throws Exception {
    router.route().handler(ctx -> {
      ctx.json(true);
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), "true");
  }

  @Test
  public void testJsonNull() throws Exception {
    router.route().handler(ctx -> {
      ctx.json(null);
    });
    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), "null");
  }

  @Test
  public void testIs() throws Exception {
    router.route().handler(event -> {
      assertTrue(event.is("json"));
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/json");
      req.write(Buffer.buffer("{ \"foo\": \"bar\" }"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void testIs2() throws Exception {
    router.route().handler(event -> {
      assertTrue(event.is("application/json"));
      event.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/json");
      req.write(Buffer.buffer("{ \"foo\": \"bar\" }"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }
  @Test
  public void testSessionAccessed() throws Exception {
    router.route().handler(event -> {
      assertFalse(event.isSessionAccessed());
      event.session();
      assertTrue(event.isSessionAccessed());
      event.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), null);
  }

  @Test
  public void test_get_or_default() throws Exception {
    router.route().handler(ctx -> {
      int foo = 1000;
      ctx.put("foo", foo);
      // ok
      Integer val = ctx.get("foo");
      assertEquals(1000, val.intValue());
      try {
        val = ctx.get("foobar");
        assertNull(val);
      } catch (RuntimeException e) {
        fail(e);
      }

      try {
        int v = ctx.get("foobar");
        fail("NPE is expected");
      } catch (NullPointerException e) {
        // OK
      }

      int foobar = ctx.getOrDefault("foobar", 1024);
      assertEquals(1024, foobar);
      ctx.response().end();
    });
    testRequest(HttpMethod.GET, "/", HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase());
  }
}
