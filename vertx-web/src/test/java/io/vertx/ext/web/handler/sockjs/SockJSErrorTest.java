package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.test.core.VertxTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

/**
 * @author Szymon Glombiowski
 */

@RunWith(VertxUnitRunner.class)
public class SockJSErrorTest extends VertxTestBase {
  public static final String EVENTBUS_ADDRESS = "addr1";
  public static final String EVENTBUS_REGISTER_MESSAGE = "{\"type\":\"register\",\"address\":\"" + EVENTBUS_ADDRESS + "\",\"headers\":{\"Accept\":\"application/json\"}}";
  public static final String EVENTBUS_UNREGISTER_MESSAGE = "{\"type\":\"unregister\",\"address\":\"" + EVENTBUS_ADDRESS + "\",\"headers\":{\"Accept\":\"application/json\"}}";
  public static final String WSS_PATH = "/wss/";
  public static final String WEBSOCKET_PATH = WSS_PATH + "websocket";
  private static final Logger log = LoggerFactory.getLogger(SockJSErrorTest.class);
  public static final int PORT = 8080;
  public static final String LOCALHOST = "localhost";
  private static int counter = 0;
  Vertx vertx;
  HttpServer server;
  private CountDownLatch countDownLatch;

  @Before
  public void before(TestContext context) {
    countDownLatch = new CountDownLatch(1);
    vertx = Vertx.vertx();

    vertx.exceptionHandler(context.exceptionHandler());
    server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(LoggerHandler.create());
    router.route("/").handler(event -> event.request().response().end("test"));

    Router sockJSRouter = createEventBusRouter();
    router.route(WSS_PATH + "*").subRouter(sockJSRouter);

    server.requestHandler(router);
    server.listen(PORT, context.asyncAssertSuccess());

    vertx.setPeriodic(100, id -> {
      log.info("server sending number: " + ++counter);
      vertx.eventBus().send(EVENTBUS_ADDRESS, counter);
    });
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testEventBusBridgeLeakingConsumers(TestContext context) throws InterruptedException {
    // initial connection - double registration and unregistration
    HttpClient client = vertx.createHttpClient();
    client.webSocket(PORT, LOCALHOST, WEBSOCKET_PATH, onSuccess(ws -> {
      ws.writeTextMessage(EVENTBUS_REGISTER_MESSAGE);
      ws.writeTextMessage(EVENTBUS_REGISTER_MESSAGE);
      // those actions will cause leak - a consumer still registered
      ws.handler(buff -> {
        log.info("websocket client 1 received raw message: " + buff.toString("UTF-8"));
        ws.writeTextMessage(EVENTBUS_UNREGISTER_MESSAGE);
        ws.writeTextMessage(EVENTBUS_UNREGISTER_MESSAGE); // this does not do anything actually, just to match 2 registrations
        ws.close();
        countDownLatch.countDown();
      });

    }));

    //make sure 1st client has completed
    countDownLatch.await();

    final int[] counter = {-1};
    HttpClient client2 = vertx.createHttpClient();
    client2.webSocket(PORT, LOCALHOST, WEBSOCKET_PATH, onSuccess(ws -> {
      ws.writeTextMessage(EVENTBUS_REGISTER_MESSAGE);
      // this client will only receive every other message
      ws.handler(buff -> {
        log.debug("websocket client 2 received raw message: " + buff.toString("UTF-8"));
        JsonObject jsonObject = new JsonObject(buff.toString("UTF-8"));
        int number = jsonObject.getInteger("body");
        log.info("websocket client 2 received number: " + number);
        // initialize - some messages might have already been handled by 1st client
        if (counter[0] == -1) {
          counter[0] = number;
        } else {
          ++counter[0];
        }
        // new number in message should be always be increased by 1
        assertEquals("Message was lost, next id not matching.", counter[0], number);

        if (number % 20 == 0) {
          testComplete();
        }
      });

    }));

    await();
  }

  @Test
  public void testEventBusBridgeLeakingConsumersClean(TestContext context) throws InterruptedException {
    // initial connection - single registration and unregistration
    HttpClient client = vertx.createHttpClient();
    client.webSocket(PORT, LOCALHOST, WEBSOCKET_PATH, onSuccess(ws -> {
      ws.writeTextMessage(EVENTBUS_REGISTER_MESSAGE);
      // those actions will cause leak - a consumer still registered
      ws.handler(buff -> {
        log.info("websocket client 1 received raw message: " + buff.toString("UTF-8"));
        ws.writeTextMessage(EVENTBUS_UNREGISTER_MESSAGE);
        ws.close();
        countDownLatch.countDown();
      });
    }));

    //make sure 1st client has completed
    countDownLatch.await();

    final int[] counter = {-1};
    HttpClient client2 = vertx.createHttpClient();
    client2.webSocket(PORT, LOCALHOST, WEBSOCKET_PATH, onSuccess(ws -> {
      ws.writeTextMessage(EVENTBUS_REGISTER_MESSAGE);
      // this client will only receive every other message
      ws.handler(buff -> {
        log.debug("websocket client 2 received raw message: " + buff.toString("UTF-8"));
        JsonObject jsonObject = new JsonObject(buff.toString("UTF-8"));
        int number = jsonObject.getInteger("body");
        log.info("websocket client 2 received number: " + number);
        // initialize - some messages might have already been handled by 1st client
        if (counter[0] == -1) {
          counter[0] = number;
        } else {
          ++counter[0];
        }
        // new number in message should be always be increased by 1
        assertEquals("Message was lost, next id not matching.", counter[0], number);

        if (number % 20 == 0) {
          testComplete();
        }
      });

    }));

    await();
  }

  private Router createEventBusRouter() {
    PermittedOptions permittedOptions = new PermittedOptions()
      .setAddress(EVENTBUS_ADDRESS);

    SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions()
      .addInboundPermitted(permittedOptions)
      .addOutboundPermitted(permittedOptions)
      .setPingTimeout(60000);  // should not interfere with test
    return SockJS.create(vertx).bridge(bridgeOptions, new TestBridgeEventHandler());
  }


  class TestBridgeEventHandler implements Handler<BridgeEvent> {
    @Override
    public void handle(BridgeEvent event) {
      JsonObject rawMessage = event.getRawMessage();
      log.debug("Bridge event type=" + event.type() + ", raw message=" + encode(rawMessage));
      event.complete(true);
    }

    private String encode(JsonObject rawMessage) {
      return rawMessage != null ? rawMessage.encode() : "null";
    }

  }
}
