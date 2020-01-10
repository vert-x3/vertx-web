package io.vertx.ext.web.handler.sse;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.impl.SSEConnectionImpl;

import java.util.List;

@VertxGen
public interface SSEConnection {

	static SSEConnection create(RoutingContext context) {
		return new SSEConnectionImpl(context);
	}

	@Fluent
  SSEConnection forward(String address);

	@Fluent
  SSEConnection forward(List<String> addresses);

	@Fluent
  SSEConnection reject(int code);

	@Fluent
  SSEConnection reject(int code, String reason);

	@Fluent
  SSEConnection comment(String comment);

	@Fluent
  SSEConnection retry(Long delay, List<String> data);

	@Fluent
  SSEConnection retry(Long delay, String data);

	@Fluent
  SSEConnection data(List<String> data);

	@Fluent
  SSEConnection data(String data);

	@Fluent
  SSEConnection event(String eventName, List<String> data);

	@Fluent
  SSEConnection event(String eventName, String data);

	@Fluent
  SSEConnection id(String id, List<String> data);

	@Fluent
  SSEConnection id(String id, String data);

	@Fluent
  SSEConnection close();

	boolean rejected();

	String lastId();

	@GenIgnore
  HttpServerRequest request();

}
