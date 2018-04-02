package io.vertx.ext.web.client;

import io.vertx.ext.web.client.impl.CachedWebClientImpl;

/**
 * @author Alexey Soshin
 */
public interface CachedWebClient extends WebClient {
    static CachedWebClient create(WebClient webClient, CachedWebClientOptions options) {
        return new CachedWebClientImpl(webClient, options);
    }
}
