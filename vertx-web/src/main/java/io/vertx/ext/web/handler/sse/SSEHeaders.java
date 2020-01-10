package io.vertx.ext.web.handler.sse;

/**
 * This is a final class to match io.vertx.core.HttpHeaders
 * Since maybe enums can cause trouble with codegen ? idk
 */
public final class SSEHeaders {

    private SSEHeaders() {}

    public static final String EVENT = "event";
    public static final String ID = "id";
    public static final String RETRY = "retry";
    public static final String LAST_EVENT_ID = "Last-Event-ID";

}
