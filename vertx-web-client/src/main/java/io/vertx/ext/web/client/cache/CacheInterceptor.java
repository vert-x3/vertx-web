package io.vertx.ext.web.client.cache;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.HttpRequest;

/**
 * Response cache implemented as WebClient interceptor
 */
public class CacheInterceptor implements Handler<HttpContext> {

    private final CacheManager cacheManager;

    public CacheInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(HttpContext event) {
        HttpRequest request = event.request();

        // Cache only GET requests
        if (!request.getMethod().equals(HttpMethod.GET)) {
            event.next();
        }
        else {
            // If this is a GET call, we should check the value in cache
            cacheManager.fetch(request,
                    // In case value in cache is still valid, just return it
                    valueFromCache -> event.getResponseHandler().handle(Future.succeededFuture(valueFromCache)),
                    // Otherwise, after the request has returned, call CacheManager with the response
                    // Probably, CacheManager would like to cache it
                    cacheMissHandler -> {
                        Handler<AsyncResult<HttpResponse<Object>>> responseHandler = event.getResponseHandler();
                        event.setResponseHandler(r -> {
                            if (r.succeeded()) {
                                cacheMissHandler.handle(r.result());
                            }
                            responseHandler.handle(r);
                        });
                        event.next();
                    });
        }
    }
}