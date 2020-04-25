package io.vertx.ext.web.api.service.generator.param_extraction;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.FilterData;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vertx.ext.web.api.service.SomeEnum.FIRST;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class HandlerParamsTest {

  private static final String ADDRESS = "address";

  private MessageConsumer<JsonObject> consumer;

  private static JsonObject buildPayload(JsonObject params) {
    return new JsonObject().put("context", new ServiceRequest(
      params,
      MultiMap.caseInsensitiveMultiMap(),
      new JsonObject(),
      null
    ).toJson());
  }

  private void testServiceEndpoint(String actionName, JsonObject params, Vertx vertx, VertxTestContext testContext) {
    RequestParametersImpl paramsToSend = new RequestParametersImpl();
    paramsToSend.setQueryParameters(
      params.getMap().entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new RequestParameterImpl(e.getValue())))
    );
    JsonObject payload = buildPayload(paramsToSend.toJson());
    String result = params
      .getMap()
      .values()
      .stream()
      .map(o -> o != null ? o : "null")
      .map(Object::toString)
      .reduce("", String::concat);
    vertx.eventBus().request(ADDRESS, payload, new DeliveryOptions().addHeader("action", actionName), (AsyncResult<Message<JsonObject>> res) -> {
      if (res.succeeded()) {
        testContext.verify(() -> {
          ServiceResponse op = new ServiceResponse(res.result().body());
          assertThat(op.getStatusCode()).isEqualTo(200);
          assertThat(op.getHeaders().get(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/plain");
          assertThat(op.getPayload().toString()).isEqualTo(result);
        });
        testContext.completeNow();
      } else {
        testContext.failNow(res.cause());
      }
    });
  }

  @BeforeEach
  public void setUp(Vertx vertx) {
    ParamsTestServiceImpl service = new ParamsTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(ADDRESS);
    consumer = serviceBinder.register(ParamsTestService.class, service);
  }

  @AfterEach
  public void tearDown(VertxTestContext testContext) {
    if (consumer != null) consumer.unregister(testContext.succeeding(v -> testContext.completeNow()));
  }

  @Test
  public void testBasicTypes(Vertx vertx, VertxTestContext testContext) throws Exception {
    testServiceEndpoint(
      "basicTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true),
      vertx, testContext
    );
  }

  @Test
  public void testBasicBoxedTypes(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "basicBoxedTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true),
      vertx, testContext
    );
  }

  @Test
  public void testBasicBoxedNullTypes(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "basicBoxedTypesNull",
      new JsonObject().putNull("str").putNull("b").putNull("s").putNull("i").putNull("l").putNull("f").putNull("d").putNull("c").putNull("bool"),
      vertx, testContext
    );
  }

  @Test
  public void testJsonTypes(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "jsonTypes",
      new JsonObject().put("jsonObject", new JsonObject().put("aaa", "a").put("bbb", "b")).put("jsonArray", new JsonArray().add("aaa").add("aaa")),
      vertx, testContext
    );
  }


  @Test
  public void testJsonTypesNull(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "jsonTypesNull",
      new JsonObject().putNull("jsonObject").putNull("jsonArray"),
      vertx, testContext
    );
  }

  @Test
  public void testEnumType(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "enumType",
      new JsonObject().put("someEnum", FIRST),
      vertx, testContext
    );
  }


  @Test
  public void testEnumTypeNull(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "enumTypeNull",
      new JsonObject().putNull("someEnum"),
      vertx, testContext
    );
  }

  @Test
  public void testDataObjectType(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "dataObjectType",
      new JsonObject().put("options", FilterData.generate().toJson()),
      vertx, testContext
    );
  }


  @Test
  public void testDataObjectTypeNull(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "dataObjectTypeNull",
      new JsonObject().putNull("options"),
      vertx, testContext
    );
  }

  @Test
  public void testListParams(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "listParams",
      new JsonObject()
        .put("listString", new JsonArray().add("aaa"))
        .put("listByte", new JsonArray().add((byte)100))
        .put("listShort", new JsonArray().add((short)101))
        .put("listInt", new JsonArray().add((int)300))
        .put("listLong", new JsonArray().add(65000l))
        .put("listJsonObject", new JsonArray().add(new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("listJsonArray", new JsonArray().add("aaa").add(102))
        .put("listDataObject", new JsonArray().add(new FilterData().setFrom(new ArrayList<>()).toJson())),
      vertx, testContext
    );
  }

  @Test
  public void testSetParams(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "setParams",
      new JsonObject()
        .put("setString", new JsonArray().add("aaa"))
        .put("setByte", new JsonArray().add((byte)100))
        .put("setShort", new JsonArray().add((short)101))
        .put("setInt", new JsonArray().add((int)300))
        .put("setLong", new JsonArray().add(65000l))
        .put("setJsonObject", new JsonArray().add(new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("setJsonArray", new JsonArray().add("aaa").add(102))
        .put("setDataObject", new JsonArray().add(new FilterData().setFrom(new ArrayList<>()).toJson())),
      vertx, testContext
    );
  }


  @Test
  public void testMapParams(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "mapParams",
      new JsonObject()
        .put("mapString", new JsonObject().put("a", "aaa"))
        .put("mapByte", new JsonObject().put("b", (byte)100))
        .put("mapShort", new JsonObject().put("c", (short)101))
        .put("mapInt", new JsonObject().put("d", (int)300))
        .put("mapLong", new JsonObject().put("e", 65000l))
        .put("mapJsonObject", new JsonObject().put("f", new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("mapJsonArray", new JsonObject().put("g", new JsonArray().add("aaa").add(102))),
      vertx, testContext
    );
  }

  @Test
  public void testFutureReturn(Vertx vertx, VertxTestContext testContext) {
    testServiceEndpoint(
      "futureReturn",
      new JsonObject().put("str", "aaa"),
      vertx, testContext
    );
  }

}
