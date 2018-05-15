package io.vertx.ext.web.client.cache;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.HttpRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Response cache implemented as WebClient interceptor
 */
public class CacheInterceptor implements Handler<HttpContext> {

    private final CacheManager cacheManager;

    private final DateFormat dateTimeFormatter;

    public CacheInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;

        this.dateTimeFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        this.dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void handle(HttpContext event) {
        HttpRequest request = event.request();

        // Cache only GET requests
        if (!request.getMethod().equals(HttpMethod.GET)) {
            event.next();
        }
        else {

            if (request.headers().get("date") == null) {
                request.putHeader("date", dateTimeFormatter.format(new Date()));
            }
            // If this is a GET call, we should check the value in cache
            Optional<HttpResponse<Object>> valueFromCache = cacheManager.fetch(request,
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

            if (valueFromCache.isPresent()) {
                event.getResponseHandler().handle(Future.succeededFuture(valueFromCache.get()));
            }
        }
    }
}