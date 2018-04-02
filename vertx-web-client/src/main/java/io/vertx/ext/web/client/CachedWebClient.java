package io.vertx.ext.web.client;

import io.vertx.ext.web.client.impl.CachedWebClientImpl;
import io.vertx.ext.web.client.impl.WebClientImpl;

/**
 * @author Alexey Soshin
 */
public interface CachedWebClient extends WebClient {
    static CachedWebClient create(WebClient webClient, CachedWebClientOptions options) {
        return new CachedWebClientImpl(webClient, options);
    }
}
