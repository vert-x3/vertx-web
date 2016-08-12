package io.vertx.ext.web.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Collections;


public class RoutingContextImplTest extends WebTestBase {

    @AfterClass
    public static void oneTimeTearDown() {
        Vertx vertx = Vertx.vertx();
        if (vertx.fileSystem().existsBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY)) {
            vertx.fileSystem().deleteRecursiveBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, true);
        }
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

}
