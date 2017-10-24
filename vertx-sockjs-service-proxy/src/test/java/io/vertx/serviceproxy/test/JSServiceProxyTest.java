package io.vertx.serviceproxy.test;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.Addresses;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.SockJSProxyTestBase;
import io.vertx.serviceproxy.testmodel.TestService;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JSServiceProxyTest extends SockJSProxyTestBase {

  TestService service;
  MessageConsumer<JsonObject> consumer;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = TestService.create(vertx);
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS);
    vertx.eventBus().<String>consumer(Addresses.TEST_ADDRESS).handler(msg -> {
      assertEquals("ok", msg.body());
      testComplete();
    });
  }

  private void deploy(String test) {
    vertx.deployVerticle(test, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      assertTrue(ar.succeeded());
    });
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    super.tearDown();
  }

  @Test
  public void testInvalidParams() {
    deploy("test_service_invalidParams.js");
    await();
  }

  @Test
  public void testNoParams() {
    deploy("test_service_noParams.js");
    await();
  }

  @Test
  public void testBasicTypes() {
    deploy("test_service_basicTypes.js");
    await();
  }

  @Test
  public void testBasicBoxedTypes() {
    deploy("test_service_basicBoxedTypes.js");
    await();
  }

  @Test
  public void testJsonTypes() {
    deploy("test_service_jsonTypes.js");
    await();
  }

  @Test
  public void testEnumType() {
    deploy("test_service_enumType.js");
    await();
  }

  @Test
  public void testDataObjectType() {
    deploy("test_service_dataObjectType.js");
    await();
  }

  @Test
  public void testListTypes() {
    deploy("test_service_listTypes.js");
    await();
  }

  @Test
  public void testSetTypes() {
    deploy("test_service_setTypes.js");
    await();
  }

  @Test
  public void testMapTypes() {
    deploy("test_service_mapTypes.js");
    await();
  }

  @Test
  public void testStringHandler() {
    deploy("test_service_stringHandler.js");
    await();
  }

  @Test
  public void testStringNullHandler() {
    deploy("test_service_stringNullHandler.js");
    await();
  }

  @Test
  public void testByteHandler() {
    deploy("test_service_byteHandler.js");
    await();
  }

  @Test
  public void testByteNullHandler() {
    deploy("test_service_byteNullHandler.js");
    await();
  }

  @Test
  public void testShortHandler() {
    deploy("test_service_shortHandler.js");
    await();
  }

  @Test
  public void testShortNullHandler() {
    deploy("test_service_shortNullHandler.js");
    await();
  }

  @Test
  public void testIntHandler() {
    deploy("test_service_intHandler.js");
    await();
  }

  @Test
  public void testIntNullHandler() {
    deploy("test_service_intNullHandler.js");
    await();
  }

  @Test
  public void testLongHandler() {
    deploy("test_service_longHandler.js");
    await();
  }

  @Test
  public void testLongNullHandler() {
    deploy("test_service_longNullHandler.js");
    await();
  }

  @Test
  public void testFloatHandler() {
    deploy("test_service_floatHandler.js");
    await();
  }

  @Test
  public void testFloatNullHandler() {
    deploy("test_service_floatNullHandler.js");
    await();
  }

  @Test
  public void testDoubleHandler() {
    deploy("test_service_doubleHandler.js");
    await();
  }

  @Test
  public void testDoubleNullHandler() {
    deploy("test_service_doubleNullHandler.js");
    await();
  }

  @Test
  public void testCharHandler() {
    deploy("test_service_charHandler.js");
    await();
  }

  @Test
  public void testCharNullHandler() {
    deploy("test_service_charNullHandler.js");
    await();
  }

  @Test
  public void testJsonObjectHandler() {
    deploy("test_service_jsonObjectHandler.js");
    await();
  }

  @Test
  public void testJsonObjectNullHandler() {
    deploy("test_service_jsonObjectNullHandler.js");
    await();
  }

  @Test
  public void testJsonArrayHandler() {
    deploy("test_service_jsonArrayHandler.js");
    await();
  }

  @Test
  public void testJsonArrayNullHandler() {
    deploy("test_service_jsonArrayNullHandler.js");
    await();
  }

  @Test
  public void testDataObjectHandler() {
    deploy("test_service_dataObjectHandler.js");
    await();
  }

  @Test
  public void testDataObjectNullHandler() {
    deploy("test_service_dataObjectNullHandler.js");
    await();
  }

  @Test
  public void testVoidHandler() {
    deploy("test_service_voidHandler.js");
    await();
  }

  @Test
  public void testFluentMethod() {
    deploy("test_service_fluentMethod.js");
    await();
  }

  @Test
  public void testFluentNoParams() {
    deploy("test_service_fluentNoParams.js");
    await();
  }

  @Test
  public void testFailingMethod() {
    deploy("test_service_failingMethod.js");
    await();
  }

  @Test
  public void testListStringHandler() {
    deploy("test_service_listStringHandler.js");
    await();
  }

  @Test
  public void testListByteHandler() {
    deploy("test_service_listByteHandler.js");
    await();
  }

  @Test
  public void testListShortHandler() {
    deploy("test_service_listShortHandler.js");
    await();
  }

  @Test
  public void testListIntHandler() {
    deploy("test_service_listIntHandler.js");
    await();
  }

  @Test
  public void testListLongHandler() {
    deploy("test_service_listLongHandler.js");
    await();
  }

  @Test
  public void testListFloatHandler() {
    deploy("test_service_listFloatHandler.js");
    await();
  }

  @Test
  public void testListDoubleHandler() {
    deploy("test_service_listDoubleHandler.js");
    await();
  }

  @Test
  public void testListCharHandler() {
    deploy("test_service_listCharHandler.js");
    await();
  }

  @Test
  public void testListBoolHandler() {
    deploy("test_service_listBoolHandler.js");
    await();
  }

  @Test
  public void testListJsonObjectHandler() {
    deploy("test_service_listJsonObjectHandler.js");
    await();
  }

  @Test
  public void testListJsonArrayHandler() {
    deploy("test_service_listJsonArrayHandler.js");
    await();
  }

  @Test
  public void testListDataObjectHandler() {
    deploy("test_service_listDataObjectHandler.js");
    await();
  }

  @Test
  public void testSetStringHandler() {
    deploy("test_service_setStringHandler.js");
    await();
  }

  @Test
  public void testSetByteHandler() {
    deploy("test_service_setByteHandler.js");
    await();
  }

  @Test
  public void testSetShortHandler() {
    deploy("test_service_setShortHandler.js");
    await();
  }

  @Test
  public void testSetIntHandler() {
    deploy("test_service_setIntHandler.js");
    await();
  }

  @Test
  public void testSetLongHandler() {
    deploy("test_service_setLongHandler.js");
    await();
  }

  @Test
  public void testSetFloatHandler() {
    deploy("test_service_setFloatHandler.js");
    await();
  }

  @Test
  public void testSetDoubleHandler() {
    deploy("test_service_setDoubleHandler.js");
    await();
  }

  @Test
  public void testSetCharHandler() {
    deploy("test_service_setCharHandler.js");
    await();
  }

  @Test
  public void testSetBoolHandler() {
    deploy("test_service_setBoolHandler.js");
    await();
  }

  @Test
  public void testSetJsonObjectHandler() {
    deploy("test_service_setJsonObjectHandler.js");
    await();
  }

  @Test
  public void testSetJsonArrayHandler() {
    deploy("test_service_setJsonArrayHandler.js");
    await();
  }

  @Test
  public void testSetDataObjectHandler() {
    deploy("test_service_setDataObjectHandler.js");
    await();
  }

  @Test
  public void testProxyIgnore() {
    deploy("test_service_proxyIgnore.js");
    await();
  }

  @Test
  public void testConnection() {
    deploy("test_service_connection.js");
    await();
  }

  @Test
  public void testConnectionTimeout() {
    consumer.unregister();
    long timeoutSeconds = 2;
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS, timeoutSeconds);
    deploy("test_service_connectionTimeout.js");
    await();
  }

  @Test
  public void testConnectionWithCloseFutureTimeout() {
    consumer.unregister();
    long timeoutSeconds = 2;
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS, timeoutSeconds);
    deploy("test_service_connectionWithCloseFutureTimeout.js");
    await();
  }

  @Test
  public void testLongDelivery1() {
    deploy("test_service_longDeliverySuccess.js");
    await();
  }

  @Test
  public void testLongDelivery2() {
    deploy("test_service_longDeliveryFailed.js");
    await();
  }
}
