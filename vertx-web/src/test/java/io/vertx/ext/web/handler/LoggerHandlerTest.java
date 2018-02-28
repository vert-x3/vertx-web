package io.vertx.ext.web.handler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author <a href="mailto:marcin.czeczko@gmail.com">Marcin Czeczko</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerHandlerImpl.class, LoggerFactory.class})
public class LoggerHandlerTest extends WebTestBase {

  @Mock
  private Logger loggerMock;

  private Date testTimestamp;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testTimestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z")
      .parse("2018/02/12 16:02:14 +0100");

    PowerMockito.mockStatic(LoggerFactory.class);
    PowerMockito.spy(System.class);
    when(LoggerFactory.getLogger(any(Class.class))).thenReturn(loggerMock);
    when(loggerMock.isDebugEnabled()).thenReturn(false);
    when(loggerMock.isTraceEnabled()).thenReturn(false);
    when(loggerMock.isInfoEnabled()).thenReturn(true);
  }

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("ACTUALLY_SYS_PROP", "test-value");
  }

  @Test
  public void test500AsError() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 500);
    verify(loggerMock, times(1)).error("500");
  }

  @Test
  public void test503AsError() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 503);
    verify(loggerMock, times(1)).error("503");
  }

  @Test
  public void test499AsWarn() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 499);
    verify(loggerMock, times(1)).warn("499");
  }

  @Test
  public void test404AsWarn() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 404);
    verify(loggerMock, times(1)).warn("404");
  }

  @Test
  public void test400AsWarn() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 400);
    verify(loggerMock, times(1)).warn("400");
  }

  @Test
  public void test399AsInfo() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 399);
    verify(loggerMock, times(1)).info("399");
  }

  @Test
  public void test304AsInfo() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 304);
    verify(loggerMock, times(1)).info("304");
  }

  @Test
  public void test200AsInfo() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));

    testLogger(logger, HttpMethod.GET, null, "", 200);
    verify(loggerMock, times(1)).info("200");
  }

  @Test
  public void testDefaultPatternTzDefault() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    given(System.currentTimeMillis()).willReturn(testTimestamp.getTime());

    LoggerHandler logger = LoggerHandler.create();
    testLogger(logger, HttpMethod.GET,
      req -> {
        req.headers().add("referer", "http://vertx.io");
        req.headers().add("user-agent", "user/agent");
      }, "123");

    verify(loggerMock, times(1)).info(
      "127.0.0.1 - - [" + sdf.format(testTimestamp)
        + "] \"GET /somedir?foo=bar HTTP/1.1\" 200 3 \"http://vertx.io\" \"user/agent\"]");
  }

  @Test
  public void testDefaultPatternTzGMT() throws Exception {
    given(System.currentTimeMillis()).willReturn(testTimestamp.getTime());
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setTimeZoneID("GMT"));
    testLogger(logger, HttpMethod.GET,
      req -> {
        req.headers().add("referer", "http://vertx.io");
        req.headers().add("user-agent", "user/agent");
      }, "123");

    verify(loggerMock, times(1)).info(
      "127.0.0.1 - - [" + sdf.format(testTimestamp)
        + "] \"GET /somedir?foo=bar HTTP/1.1\" 200 3 \"http://vertx.io\" \"user/agent\"]");
  }

  @Test
  public void testInvalidParametrizedParms() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("START-%C%-%e-%i-%o-%M-END"));
    testLogger(logger, HttpMethod.GET, null, "123");

    verify(loggerMock, times(1)).info(
      "START------END");
  }

  @Test
  public void testClientIpParam() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%a"));
    testLogger(logger, HttpMethod.GET, null, "123");

    verify(loggerMock, times(1)).info(
      "127.0.0.1");
  }

  @Test
  public void testClientHostParam() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%h"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "127.0.0.1");
  }

  @Test
  public void testLocalIpParam() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%A"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "127.0.0.1");
  }

  @Test
  public void testLowerCaseByteCountFormatNonZeroLength() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%b"));
    testLogger(logger, HttpMethod.GET, null, "1234567890");

    verify(loggerMock, times(1)).info(
      "10");
  }

  @Test
  public void testLowerCaseByteCountFormatZeroLength() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%b"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "-");
  }

  @Test
  public void testUpperCaseByteCountFormatNonZeroLength() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%B"));
    testLogger(logger, HttpMethod.GET, null, "1234567890");

    verify(loggerMock, times(1)).info(
      "10");
  }

  @Test
  public void testUpperCaseByteCountFormatZeroLength() throws Exception {
    LoggerHandler logger = LoggerHandler.create(new LoggerHandlerOptions().setPattern("%B"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "0");
  }

  @Test
  public void testLowerCaseByteCountFormatNonZeroLengthImmediate() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%b").setImmediate(true));
    String body = "1234567890";
    testLogger(logger, HttpMethod.POST,
      req -> {
        req.headers().add("content-length", Long.toString(body.length()));
        req.write(body);
      }, body);

    verify(loggerMock, times(1)).info(
      "10");
  }

  @Test
  public void testLowerCaseByteCountFormatZeroLengthImmediate() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%b").setImmediate(true));
    String body = "";
    testLogger(logger, HttpMethod.POST,
      req -> {
        req.headers().add("content-length", Long.toString(body.length()));
        req.write(body);
      }, body);

    verify(loggerMock, times(1)).info(
      "-");
  }

  @Test
  public void testUpperCaseByteCountFormatNonZeroLengthImmediate() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%B").setImmediate(true));
    String body = "1234567890";
    testLogger(logger, HttpMethod.POST,
      req -> {
        req.headers().add("content-length", Long.toString(body.length()));
        req.write(body);
      }, body);

    verify(loggerMock, times(1)).info(
      "10");
  }

  @Test
  public void testUpperCaseByteCountFormatZeroLengthImmediate() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%B").setImmediate(true));
    String body = "";
    testLogger(logger, HttpMethod.POST,
      req -> {
        req.headers().add("content-length", Long.toString(body.length()));
        req.write(body);
      }, body);

    verify(loggerMock, times(1)).info(
      "0");
  }

  @Test
  public void testCookieParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{my-cookie}C"));
    testLogger(logger, HttpMethod.GET,
      req -> req.headers().add("cookie", "my-cookie=value"), "");

    verify(loggerMock, times(1)).info(
      "my-cookie=value");
  }

  @Test
  public void testDurationParamTwoVersions() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%D%T"));
    testDelay(logger, 200);

    //A tricky part. Expect a duration to be around between 100 and 199 ms, twice (for D and T param)
    verify(loggerMock, times(1)).info(
      matches("(2\\d\\d)\\1"));

    router.clear();
    testDelay(logger, 100);
    verify(loggerMock, times(1)).info(
      matches("(1\\d\\d)\\1"));
  }

  @Test
  public void testEnvParamEnvVariable() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{TEST_VAR}e"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "test-value");
  }

  @Test
  public void testEnvParamSysPropFallback() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{ACTUALLY_SYS_PROP}e"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "test-value");
  }


  @Test
  public void testProtocolParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%H"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "HTTP/1.1");
  }

  @Test
  public void testRequestHeaderParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{referer}i"));
    testLogger(logger, HttpMethod.GET, req -> req.headers().add("referer", "http://vertx.io"), "");

    verify(loggerMock, times(1)).info(
      "http://vertx.io");
  }

  @Test
  public void testMethodParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%m"));
    testLogger(logger, HttpMethod.HEAD, null, "");

    verify(loggerMock, times(1)).info(
      "HEAD");
  }

  @Test
  public void testParamParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{foo}M"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "bar");
  }

  @Test
  public void testResponseHeaderParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{Content-Length}o"));
    testLogger(logger, HttpMethod.GET, null, "123456");

    verify(loggerMock, times(1)).info(
      "6");
  }

  @Test
  public void testLocalPortParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%p"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      "8080");
  }

  @Test
  public void testThreadParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%P"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(
      matches("vert.x-eventloop-thread-.+"));
  }

  @Test
  public void testQueryParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%q"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("?foo=bar");
  }

  @Test
  public void testRequestFirstLineParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%r"));
    testLogger(logger, HttpMethod.OPTIONS, null, "");

    verify(loggerMock, times(1)).info("OPTIONS /somedir?foo=bar HTTP/1.1");
  }

  @Test
  public void testStatusParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%s"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("200");
  }

  @Test
  public void testTimeParamDefault() throws Exception {
    given(System.currentTimeMillis()).willReturn(testTimestamp.getTime());
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%t"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(sdf.format(testTimestamp));
  }

  @Test
  public void testTimeParamCustomFormat() throws Exception {
    given(System.currentTimeMillis()).willReturn(testTimestamp.getTime());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%{%F %r}t"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info(sdf.format(testTimestamp));
  }

  @Test
  public void testUrlPathParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%U"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("/somedir");
  }

  @Test
  public void testServerNameParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%v %V"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("localhost localhost");
  }

  @Test
  public void testUnsupportedParam() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("%G"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("-");
  }

  @Test
  public void testEscapePercent() throws Exception {
    LoggerHandler logger = LoggerHandler
      .create(new LoggerHandlerOptions().setPattern("-%%-"));
    testLogger(logger, HttpMethod.GET, null, "");

    verify(loggerMock, times(1)).info("-%-");
  }

  private void testDelay(LoggerHandler logger, long delayMs)
    throws Exception {
    router.route().handler(logger);
    router.route().handler(rc -> vertx.setTimer(delayMs, x -> rc.response().end()));
    testRequest(HttpMethod.GET, "/somedir?foo=bar", 200, "OK");
  }

  private void testLogger(LoggerHandler logger, HttpMethod method,
    Consumer<HttpClientRequest> req, String responseBody) throws Exception {
    testLogger(logger, method, req, responseBody, 200);
  }

  private void testLogger(LoggerHandler logger, HttpMethod method, Consumer<HttpClientRequest> req,
    String responseBody, int statusCode)
    throws Exception {
    router.route().handler(logger);
    router.route().handler(rc -> rc.response().setStatusCode(statusCode).end(responseBody));
    testRequest(method, "/somedir?foo=bar", req, statusCode,
      HttpResponseStatus.valueOf(statusCode).reasonPhrase(), responseBody);
  }

}
