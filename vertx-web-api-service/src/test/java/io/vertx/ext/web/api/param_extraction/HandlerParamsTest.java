package io.vertx.ext.web.api.param_extraction;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.impl.RequestParameterImpl;
import io.vertx.ext.web.api.impl.RequestParametersImpl;
import io.vertx.ext.web.api.router_factory_integration.FilterData;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static io.vertx.ext.web.api.router_factory_integration.SomeEnum.FIRST;


public class HandlerParamsTest extends VertxTestBase {

  private static final String ADDRESS = "address";

  private static JsonObject buildPayload(JsonObject params) {
    return new JsonObject().put("context", new OperationRequest(
      params,
      MultiMap.caseInsensitiveMultiMap(),
      new JsonObject(),
      null
    ).toJson());
  }

  private void testServiceEndpoint(String address, String actionName, JsonObject params) throws Exception {
    RequestParametersImpl paramsToSend = new RequestParametersImpl();
    paramsToSend.setFormParameters(
      params.getMap().entrySet()
        .stream()
      .map(e -> new SimpleEntry<>(e.getKey(), new RequestParameterImpl(e.getKey(), e.getValue())))
      .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
    );
    JsonObject payload = buildPayload(paramsToSend.toJson());
    String result = params
      .getMap().entrySet()
      .stream().map(e -> e.getValue() != null ? e.getValue() : "null").map(Object::toString).reduce("", String::concat);
    CountDownLatch latch = new CountDownLatch(1);
    vertx.eventBus().send(address, payload, new DeliveryOptions().addHeader("action", actionName), (AsyncResult<Message<JsonObject>> res) -> {
      if (res.succeeded()) {
        OperationResponse op = new OperationResponse(res.result().body());
        assertEquals(200, op.getStatusCode().intValue());
        assertEquals("text/plain", op.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertEquals(result, op.getPayload().toString());
      } else {
        assertTrue("Error during service call: " + res.cause(), false);
      }
      latch.countDown();
    });
    latch.await();
  }

  private void mountAndTest(String address, String actionName, JsonObject params) throws Exception {
    ParamsTestServiceImpl service = new ParamsTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(address);
    MessageConsumer<JsonObject> consumer = serviceBinder.register(ParamsTestService.class, service);
    testServiceEndpoint(address, actionName, params);
    serviceBinder.unregister(consumer);
  }

  @Test
  public void testBasicTypes() throws Exception {
    mountAndTest(
      ADDRESS,
      "basicTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true)
    );
  }

  @Test
  public void testBasicBoxedTypes() throws Exception {
    mountAndTest(
      ADDRESS,
      "basicBoxedTypes",
      new JsonObject().put("str", "aaa").put("b", (byte)100).put("s", (short)10).put("i", (int)101).put("l", 102l).put("f", 102.2f).put("d", 102.5d).put("c", 'C').put("bool", true)
    );
  }

  @Test
  public void testBasicBoxedNullTypes() throws Exception {
    mountAndTest(
      ADDRESS,
      "basicBoxedTypesNull",
      new JsonObject().putNull("str").putNull("b").putNull("s").putNull("i").putNull("l").putNull("f").putNull("d").putNull("c").putNull("bool")
    );
  }

  @Test
  public void testJsonTypes() throws Exception {
    mountAndTest(
      ADDRESS,
      "jsonTypes",
      new JsonObject().put("jsonObject", new JsonObject().put("aaa", "a").put("bbb", "b")).put("jsonArray", new JsonArray().add("aaa").add("aaa"))
    );
  }


  @Test
  public void testJsonTypesNull() throws Exception {
    mountAndTest(
      ADDRESS,
      "jsonTypesNull",
      new JsonObject().putNull("jsonObject").putNull("jsonArray")
    );
  }

  @Test
  public void testEnumType() throws Exception {
    mountAndTest(
      ADDRESS,
      "enumType",
      new JsonObject().put("someEnum", FIRST)
    );
  }


  @Test
  public void testEnumTypeNull() throws Exception {
    mountAndTest(
      ADDRESS,
      "enumTypeNull",
      new JsonObject().putNull("someEnum")
    );
  }

  @Test
  public void testDataObjectType() throws Exception {
    mountAndTest(
      ADDRESS,
      "dataObjectType",
      new JsonObject().put("options", FilterData.generate().toJson())
    );
  }


  @Test
  public void testDataObjectTypeNull() throws Exception {
    mountAndTest(
      ADDRESS,
      "dataObjectTypeNull",
      new JsonObject().putNull("options")
    );
  }

  @Test
  public void testListParams() throws Exception {
    mountAndTest(
      ADDRESS,
      "listParams",
      new JsonObject()
        .put("listString", new JsonArray().add("aaa"))
        .put("listByte", new JsonArray().add((byte)100))
        .put("listShort", new JsonArray().add((short)101))
        .put("listInt", new JsonArray().add((int)300))
        .put("listLong", new JsonArray().add(65000l))
        .put("listJsonObject", new JsonArray().add(new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("listJsonArray", new JsonArray().add("aaa").add(102))
        .put("listDataObject", new JsonArray().add(new FilterData().setFrom(new ArrayList<>()).toJson()))
    );
  }

  @Test
  public void testSetParams() throws Exception {
    mountAndTest(
      ADDRESS,
      "setParams",
      new JsonObject()
        .put("setString", new JsonArray().add("aaa"))
        .put("setByte", new JsonArray().add((byte)100))
        .put("setShort", new JsonArray().add((short)101))
        .put("setInt", new JsonArray().add((int)300))
        .put("setLong", new JsonArray().add(65000l))
        .put("setJsonObject", new JsonArray().add(new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("setJsonArray", new JsonArray().add("aaa").add(102))
        .put("setDataObject", new JsonArray().add(new FilterData().setFrom(new ArrayList<>()).toJson()))
    );
  }


  @Test
  public void testMapParams() throws Exception {
    mountAndTest(
      ADDRESS,
      "mapParams",
      new JsonObject()
        .put("mapString", new JsonObject().put("a", "aaa"))
        .put("mapByte", new JsonObject().put("b", (byte)100))
        .put("mapShort", new JsonObject().put("c", (short)101))
        .put("mapInt", new JsonObject().put("d", (int)300))
        .put("mapLong", new JsonObject().put("e", 65000l))
        .put("mapJsonObject", new JsonObject().put("f", new JsonObject().put("aaa", "a").put("bbb", "b")))
        .put("mapJsonArray", new JsonObject().put("g", new JsonArray().add("aaa").add(102)))
    );
  }

}
