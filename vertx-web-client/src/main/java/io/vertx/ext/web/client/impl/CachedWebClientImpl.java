package io.vertx.ext.web.client.impl;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Alexey Soshin
 */
public class CachedWebClientImpl implements CachedWebClient {
    private final WebClientImpl client;
    private final CachedWebClientOptions options;

    public CachedWebClientImpl(WebClient webClient, CachedWebClientOptions options) {
        if (!(webClient instanceof WebClientImpl)) {
            throw new RuntimeException("Can only wrap WebClientImpl");
        }
        this.client = (WebClientImpl) webClient;
        if (options == null) {
            options = new CachedWebClientOptions();
        }
        this.options = options;

        this.client.addInterceptor(new CacheInterceptor());
    }

    @Override
    public HttpRequest<Buffer> get(int port, String host, String requestURI) {
        return client.get(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String requestURI) {
        return client.get(requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String host, String requestURI) {
        return client.get(host, requestURI);
    }

    public static WebClient create(Vertx vertx) {
        return WebClient.create(vertx);
    }

    @Override
    public HttpRequest<Buffer> getAbs(String absoluteURI) {
        return client.getAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> post(String requestURI) {
        return client.post(requestURI);
    }

    public static WebClient create(Vertx vertx, WebClientOptions options) {
        return WebClient.create(vertx, options);
    }

    @Override
    public HttpRequest<Buffer> post(String host, String requestURI) {
        return client.post(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> post(int port, String host, String requestURI) {
        return client.post(port, host, requestURI);
    }

    public static WebClient wrap(HttpClient httpClient) {
        return WebClient.wrap(httpClient);
    }

    @Override
    public HttpRequest<Buffer> put(String requestURI) {
        return client.put(requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(String host, String requestURI) {
        return client.put(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(int port, String host, String requestURI) {
        return client.put(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(String host, String requestURI) {
        return client.delete(host, requestURI);
    }

    public static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
        return WebClient.wrap(httpClient, options);
    }

    @Override
    public HttpRequest<Buffer> delete(String requestURI) {
        return client.delete(requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
        return client.delete(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String requestURI) {
        return client.patch(requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String host, String requestURI) {
        return client.patch(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
        return client.patch(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String requestURI) {
        return client.head(requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String host, String requestURI) {
        return client.head(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(int port, String host, String requestURI) {
        return client.head(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> postAbs(String absoluteURI) {
        return client.postAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> putAbs(String absoluteURI) {
        return client.putAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
        return client.deleteAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> patchAbs(String absoluteURI) {
        return client.patchAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> headAbs(String absoluteURI) {
        return client.headAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
        return client.request(method, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
        return client.request(method, requestOptions);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
        return client.request(method, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
        return client.request(method, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
        return client.requestAbs(method, surl);
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     *
     */
    private class CacheInterceptor implements Handler<HttpContext> {

        private final Map<String, HttpResponse<Object>> cache = new ConcurrentHashMap<>();

        @Override
        public void handle(HttpContext event) {
            HttpRequestImpl request = (HttpRequestImpl) event.request();

            // Cache only GET requests
            if (!request.method.equals(HttpMethod.GET)) {
                event.next();
            }
            else {
                String cacheKey = generateKey(request);
                System.out.println(cacheKey);
                if (cache.containsKey(cacheKey)) {
                    HttpResponse<Object> cacheValue = cache.get(cacheKey);
                    event.getResponseHandler().handle(Future.succeededFuture(cacheValue));
                }
                else {
                    Handler<AsyncResult<HttpResponse<Object>>> responseHandler = event.getResponseHandler();
                    event.setResponseHandler(ar -> {
                        HttpResponse<Object> response = ar.result();
                        if (ar.succeeded()) {
                            cache.put(cacheKey, response);
                        }
                        responseHandler.handle(ar);
                    });
                    event.next();
                }
            }
        }

        private String generateKey(HttpRequestImpl request) {
            StringBuilder sb = new StringBuilder();
            sb.append(request.method);
            sb.append(request.host);
            sb.append(request.port);
            sb.append(request.uri);
            // Concatenate all query params
            String params = StreamSupport.stream(request.queryParams().spliterator(), false).
                    sorted().
                    map(Object::toString).
                    collect(Collectors.joining());
            sb.append(params);
            return sb.toString();
        }
    }
}
