package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderParameter extends BaseParameter {

  private String headerName;
  private final boolean isRequest;

  public HeaderParameter(String headerName, boolean isRequest) {
    super(headerName);
    this.headerName = headerName;
    this.isRequest = isRequest;
  }

  @Override
  protected String getValue(RoutingContext context) {
    List<String> headerValues;
    if (isRequest) {
      headerValues = context.request().headers().getAll(headerName);
    } else {
      headerValues = context.response().headers().getAll(headerName);
    }

    if (headerValues == null || headerValues.isEmpty()) {
      return null;
    }

    return escape(headerValues.stream().collect(Collectors.joining(",")));
  }
}
