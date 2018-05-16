package io.vertx.ext.web.client.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Response cache implemented as WebClient interceptor
 */
public class CacheInterceptor implements Handler<HttpContext> {

    private final CacheManager cacheManager;

    private final DateFormat dateTimeFormatter;

    CacheInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;

        this.dateTimeFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        this.dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void handle(HttpContext event) {
        HttpRequest request = event.request();

        HttpMethod method = ((HttpRequestImpl) request).method;

        // Cache only GET requests
        if (!method.equals(HttpMethod.GET)) {
            event.next();
        }
        else {

            if (request.headers().get("date") == null) {
                request.putHeader("date", dateTimeFormatter.format(new Date()));
            }
            // If this is a GET call, we should check the value in cache
            Optional<HttpResponse<Object>> valueFromCache = cacheManager.fetch(request);

            if (valueFromCache.isPresent()) {
                HttpResponse<Object> value = valueFromCache.get();
                Set<String> cacheControl = parseCacheControl(request);
                if (shouldUseCache(cacheControl)) {
                    if (!isValueExpired(request, cacheControl, value)) {
                        event.getResponseHandler().handle(Future.succeededFuture(valueFromCache.get()));
                        return;
                    }
                    else {
                        cacheManager.remove(request);
                    }
                }
            }

            handleCacheMiss(event, request);
        }
    }

    private void handleCacheMiss(HttpContext event, HttpRequest request) {
        Handler<AsyncResult<HttpResponse<Object>>> responseHandler = event.getResponseHandler();
        event.setResponseHandler(r -> {
            if (r.succeeded()) {
                cacheManager.put(request, r.result());
            }
            responseHandler.handle(r);
        });
        event.next();
    }

    private boolean shouldUseCache(Set<String> cacheControl) {
        if (cacheControl.isEmpty()) {
            return true;
        }

        if (cacheControl.contains("public")) {
            return true;
        }

        return false;
    }

    /**
     * Creates a set of values out of Cache-Control header
     * @param request
     * @return
     */
    private Set<String> parseCacheControl(HttpRequest request) {
        String cacheControlValue = request.headers().get("cache-control");

        if (cacheControlValue == null) {
            return Collections.emptySet();
        }

        return Arrays.stream(cacheControlValue.split(",")).
                filter(Objects::nonNull).
                map(String::trim).
                map(String::toLowerCase).
                collect(Collectors.toSet());
    }

    /**
     * Validates ETag and max-age of the value in cache
     * @param request
     * @param cacheControl
     * @param cacheValue
     * @return
     */
    private boolean isValueExpired(HttpRequest request,
                                   Set<String> cacheControl,
                                   HttpResponse<Object> cacheValue) {
        if (!isValidEtag(request, cacheValue)) {
            return true;
        }

        Optional<Integer> maxAge = parseMaxAge(cacheControl);

        // Cannot expire if not set
        if (!maxAge.isPresent()) {
            return false;
        }

        try {
            String dateHeaderValue = cacheValue.getHeader("date");
            if (dateHeaderValue == null) {
                return false;
            }
            Date date = dateTimeFormatter.parse(dateHeaderValue);
            long current = System.currentTimeMillis();

            // Check that this entry has not expired if age was set
            return current > date.getTime() + maxAge.get();
        }
        catch (ParseException pe) {
            return false;
        }
    }

    /**
     * Compares ETag header value from the request with the one cached
     * @param request
     * @param cacheValue
     * @return true if one of the ETags is empty, or both equals
     *         false if both present but not equal
     */
    private boolean isValidEtag(HttpRequest request, HttpResponse<Object> cacheValue) {
        String requestEtag = request.headers().get("ETag");

        if (requestEtag == null) {
            return true;
        }

        String responseEtag = cacheValue.headers().get("ETag");

        if (responseEtag == null) {
            return true;
        }

        // If ETags are different, value should be considered expired
        if  (!responseEtag.equals(requestEtag)) {
            return false;
        }

        return true;
    }

    /**
     * Gets max-age header value if exists
     * @param cacheControl set of key=value pairs of Cache-Control header
     * @return optional representing value of max-age part
     */
    private Optional<Integer> parseMaxAge(Set<String> cacheControl) {
        Optional<String> maxAge = cacheControl.stream().
                filter(c -> c.startsWith("max-age")).
                findFirst();

        if (maxAge.isPresent()) {
            String[] maxAgeParts = maxAge.get().split("=");
            if (maxAgeParts.length > 1) {
                try {
                    return Optional.of(Integer.parseInt(maxAgeParts[1]));
                }
                catch (NumberFormatException nfe) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }
}