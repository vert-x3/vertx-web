package io.vertx.ext.web.api.service.tests.generator.param_extraction;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.tests.FilterData;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vertx.ext.web.api.service.tests.SomeEnum.FIRST;
import static org.assertj.core.api.Assertions.assertThat;

@VertxTest
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

  private void testServiceEndpoint(String actionName, JsonObject params, Vertx vertx) {
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
    Message<JsonObject> response = vertx.eventBus()
      .<JsonObject>request(ADDRESS, payload, new DeliveryOptions().addHeader("action", actionName))
      .await();
    ServiceResponse op = new ServiceResponse(response.body());
    assertThat(op.getStatusCode()).isEqualTo(200);
    assertThat(op.getHeaders().get(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/plain");
    assertThat(op.getPayload().toString()).isEqualTo(result);
  }

  @BeforeEach
  public void setUp(Vertx vertx) {
    ParamsTestServiceImpl service = new ParamsTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(ADDRESS);
    consumer = serviceBinder.register(ParamsTestService.class, service);
  }

  @AfterEach
  public void tearDown() {
    if (consumer != null) {
      consumer
        .unregister()
        .await();
    };
  }

  @Test
  public void testBasicTypes(Vertx vertx) throws Exception {
    testServiceEndpoint(
      "basicTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true),
      vertx
    );
  }

  @Test
  public void testBasicBoxedTypes(Vertx vertx) {
    testServiceEndpoint(
      "basicBoxedTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true),
      vertx
    );
  }

  @Test
  public void testBasicBoxedNullTypes(Vertx vertx) {
    testServiceEndpoint(
      "basicBoxedTypesNull",
      new JsonObject().putNull("str").putNull("b").putNull("s").putNull("i").putNull("l").putNull("f").putNull("d").putNull("c").putNull("bool"),
      vertx
    );
  }

  @Test
  public void testJsonTypes(Vertx vertx) {
    testServiceEndpoint(
      "jsonTypes",
      new JsonObject().put("jsonObject", new JsonObject().put("aaa", "a").put("bbb", "b")).put("jsonArray", new JsonArray().add("aaa").add("aaa")),
      vertx
    );
  }


  @Test
  public void testJsonTypesNull(Vertx vertx) {
    testServiceEndpoint(
      "jsonTypesNull",
      new JsonObject().putNull("jsonObject").putNull("jsonArray"),
      vertx
    );
  }

  @Test
  public void testEnumType(Vertx vertx) {
    testServiceEndpoint(
      "enumType",
      new JsonObject().put("someEnum", FIRST),
      vertx
    );
  }


  @Test
  public void testEnumTypeNull(Vertx vertx) {
    testServiceEndpoint(
      "enumTypeNull",
      new JsonObject().putNull("someEnum"),
      vertx
    );
  }

  @Test
  public void testDataObjectType(Vertx vertx) {
    testServiceEndpoint(
      "dataObjectType",
      new JsonObject().put("options", FilterData.generate().toJson()),
      vertx
    );
  }


  @Test
  public void testDataObjectTypeNull(Vertx vertx) {
    testServiceEndpoint(
      "dataObjectTypeNull",
      new JsonObject().putNull("options"),
      vertx
    );
  }

  @Test
  public void testListParams(Vertx vertx) {
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
      vertx
    );
  }

  @Test
  public void testSetParams(Vertx vertx) {
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
      vertx
    );
  }


  @Test
  public void testMapParams(Vertx vertx) {
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
      vertx
    );
  }

}
