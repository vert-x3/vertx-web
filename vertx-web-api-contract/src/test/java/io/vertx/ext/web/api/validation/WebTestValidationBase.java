package io.vertx.ext.web.api.validation;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.ApiWebTestBase;
import io.vertx.ext.web.handler.BodyHandler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class WebTestValidationBase extends ApiWebTestBase {

  static Map<ParameterType, List<String>> sampleValuesSuccess;
  static Map<ParameterType, List<String>> sampleValuesFailure;

  static {
    sampleValuesSuccess = new HashMap<>();
    sampleValuesFailure = new HashMap<>();

    sampleValuesSuccess.put(ParameterType.BOOL, Arrays.asList("true", "false", "1", "0", "True", "False", "TRUE",
      "FALSE"));
    sampleValuesFailure.put(ParameterType.BOOL, Arrays.asList("trues", "yes", "no", "ok"));
    sampleValuesSuccess.put(ParameterType.EMAIL, Arrays.asList("vertx@vertx.io", "awesome.vertx@vert.vertx.io",
      "random_email@vertx.io"));
    sampleValuesFailure.put(ParameterType.EMAIL, Arrays.asList("vertx.io", "@vertx.com"));
    sampleValuesSuccess.put(ParameterType.URI, Arrays.asList("ftp://awesomeftp/file.txt", "http://vertx.io",
      "mailto:vertx@vertx.io", "irc://irc.freenode.net/vertx.io"));
    sampleValuesFailure.put(ParameterType.URI, Arrays.asList("ftpvertx"));
    sampleValuesSuccess.put(ParameterType.INT, Arrays.asList("1000", "156123"));
    sampleValuesFailure.put(ParameterType.INT, Arrays.asList("adsf465", "156.526", "45 564"));
    sampleValuesSuccess.put(ParameterType.FLOAT, Arrays.asList("156.56", "49876.465"));
    sampleValuesFailure.put(ParameterType.FLOAT, Arrays.asList("anui34j52"));
    sampleValuesSuccess.put(ParameterType.DOUBLE, Arrays.asList("156.56", "49876.465"));
    sampleValuesFailure.put(ParameterType.DOUBLE, Arrays.asList("fr1564", "4564, 516", "465f, ge78"));
    sampleValuesSuccess.put(ParameterType.DATE, Arrays.asList(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
    sampleValuesFailure.put(ParameterType.DATE, Arrays.asList(new SimpleDateFormat("yy-MM-dd").format(new Date())));
    sampleValuesSuccess.put(ParameterType.DATETIME, Arrays.asList(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
      .format(new Date())));
    sampleValuesFailure.put(ParameterType.DATETIME, Arrays.asList(new SimpleDateFormat("yy-MM-dd").format(new Date())
      , new SimpleDateFormat("HH:mm:ssXXX").format(new Date())));
    sampleValuesSuccess.put(ParameterType.TIME, Arrays.asList(new SimpleDateFormat("HH:mm:ss").format(new Date())));
    sampleValuesFailure.put(ParameterType.TIME, Arrays.asList(new SimpleDateFormat("yyyy:MM:dd").format(new Date())));
    sampleValuesSuccess.put(ParameterType.BASE64, Arrays.asList("SGVsbG8gVmVydHg="));
    sampleValuesFailure.put(ParameterType.BASE64, Arrays.asList());
    sampleValuesSuccess.put(ParameterType.UUID, Arrays.asList(UUID.randomUUID().toString()));
    sampleValuesFailure.put(ParameterType.UUID, Arrays.asList(UUID.randomUUID().toString() + "a"));

  }

  public RequestParameter getSuccessSample(ParameterType type) {
    int i = ThreadLocalRandom.current().nextInt(0, WebTestValidationBase.sampleValuesSuccess.get(type).size());
    return type.validationMethod().isValid(WebTestValidationBase.sampleValuesSuccess.get(type).get(i));
  }

  public String getFailureSample(ParameterType type) {
    int i = ThreadLocalRandom.current().nextInt(0, WebTestValidationBase.sampleValuesFailure.get(type).size());
    return WebTestValidationBase.sampleValuesFailure.get(type).get(i);
  }

  public void loadHandlers(String path, HttpMethod method, boolean expectFail, ValidationHandler validationHandler,
                           Handler<RoutingContext> handler) {
    router.route(method, path).handler(BodyHandler.create())
      .handler(validationHandler)
      .handler(handler)
      .failureHandler(generateFailureHandler(expectFail));
  }

  public void loadHandlers(String path, HttpMethod method, boolean expectFail, ValidationHandler validationHandler,
                           Handler<RoutingContext> handler, Handler<RoutingContext> failureHandler) {
    router.route(method, path).handler(BodyHandler.create())
      .handler(validationHandler)
      .handler(handler)
      .failureHandler(failureHandler);
  }

  public void testPrimitiveParameterType(ParameterType type) {
    List<String> sampleValuesSuccess = WebTestValidationBase.sampleValuesSuccess.get(type);
    List<String> sampleValuesFailure = WebTestValidationBase.sampleValuesFailure.get(type);

    for (String s : sampleValuesSuccess) {
      Boolean valid;
      try {
        type.validationMethod().isValid(s);
        valid = true;
      } catch (ValidationException e) {
        valid = false;
      }
      assertTrue("Error with string: " + s, valid);
    }

    for (String s : sampleValuesFailure) {
      Boolean valid;
      try {
        type.validationMethod().isValid(s);
        valid = true;
      } catch (ValidationException e) {
        valid = false;
      }
      assertFalse("Error with string: " + s, valid);
    }
  }

  public Handler<RoutingContext> generateFailureHandler(boolean expected) {
    return routingContext -> {
      Throwable failure = routingContext.failure();
      if (failure instanceof ValidationException) {
        if (!expected) {
          failure.printStackTrace();
        }
        routingContext.response().setStatusCode(400).setStatusMessage("failure:" + ((ValidationException) failure)
          .type().name()).end();
      } else {
        failure.printStackTrace();
        routingContext.response().setStatusCode(500).setStatusMessage("unknownfailure:" + failure.toString()).end();
      }
    };
  }

  public String errorMessage(ValidationException.ErrorType error) {
    return "failure:" + error.name();
  }

  public String serializeInCSVStringArray(List<String> values) {
    return this.serializeStringArray(values, ",");
  }

  public String serializeStringArray(List<String> values, String separator) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String s : values) {
      stringBuilder.append(s + separator);
    }
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    return stringBuilder.toString();
  }

}
