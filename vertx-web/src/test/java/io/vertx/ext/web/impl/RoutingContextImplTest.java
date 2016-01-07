package io.vertx.ext.web.impl;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.util.Collections;


public class RoutingContextImplTest extends WebTestBase {

    private RoutingContextImpl routingContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        routingContext = new RoutingContextImpl("localhost", (RouterImpl) router, new MockRequest(), Collections.EMPTY_SET);
    }

    @Test
    public void test_empty_array_as_json_array_yields_empty_json_array() {
        routingContext.setBody(Buffer.buffer("[]"));
        assertEquals(new JsonArray(), routingContext.getBodyAsJsonArray());
    }

    @Test
    public void test_one_item_array_as_json_array_yields_one_item_json_array() {
        JsonArray array = new JsonArray(
                Collections.singletonList(
                        new JsonObject(Collections.singletonMap("a", "b"))
                )
        );
        routingContext.setBody(Buffer.buffer("[ { \"a\": \"b\" } ]"));

        assertEquals(array, routingContext.getBodyAsJsonArray());
    }

    @Test
    public void test_empty_object_as_json_yields_empty_json_object() {
        routingContext.setBody(Buffer.buffer("{}"));
        assertEquals(new JsonObject(), routingContext.getBodyAsJson());
    }

    @Test
    public void test_object_as_json_yields_json_object() {
        routingContext.setBody(Buffer.buffer("{ \"a\": \"b\" }"));
        assertEquals(new JsonObject(Collections.singletonMap("a", "b")), routingContext.getBodyAsJson());
    }

    private class MockRequest implements io.vertx.core.http.HttpServerRequest {

        @Override
        public io.vertx.core.http.HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest handler(Handler<Buffer> handler) {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest pause() {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest resume() {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest endHandler(Handler<Void> endHandler) {
            return null;
        }

        @Override
        public HttpVersion version() {
            return null;
        }

        @Override
        public HttpMethod method() {
            return HttpMethod.GET;
        }

        @Override
        public boolean isSSL() {
            return false;
        }

        @Override
        public String uri() {
            return null;
        }

        @Override
        public String path() {
            return "/test";
        }

        @Override
        public String query() {
            return null;
        }

        @Override
        public HttpServerResponse response() {
            return null;
        }

        @Override
        public MultiMap headers() {
            return null;
        }

        @Override
        public String getHeader(String headerName) {
            return null;
        }

        @Override
        public String getHeader(CharSequence headerName) {
            return null;
        }

        @Override
        public MultiMap params() {
            return null;
        }

        @Override
        public String getParam(String paramName) {
            return null;
        }

        @Override
        public SocketAddress remoteAddress() {
            return null;
        }

        @Override
        public SocketAddress localAddress() {
            return null;
        }

        @Override
        public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
            return new X509Certificate[0];
        }

        @Override
        public String absoluteURI() {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
            return null;
        }

        @Override
        public NetSocket netSocket() {
            return null;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest setExpectMultipart(boolean expect) {
            return null;
        }

        @Override
        public boolean isExpectMultipart() {
            return false;
        }

        @Override
        public io.vertx.core.http.HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
            return null;
        }

        @Override
        public MultiMap formAttributes() {
            return null;
        }

        @Override
        public String getFormAttribute(String attributeName) {
            return null;
        }

        @Override
        public ServerWebSocket upgrade() {
            return null;
        }

        @Override
        public boolean isEnded() {
            return false;
        }
    }
}
