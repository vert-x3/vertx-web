package io.vertx.ext.web.validation;

import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.validation.impl.OpenAPI3RequestValidationHandlerImpl;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3ValidationTest extends WebTestValidationBase {

  private OpenApi3 loadSwagger(String filename) {
    return (OpenApi3) new OpenApiParser().parse(new File(filename), true);
  }

  @Test
  public void loadSampleOperationObject() throws Exception {
    OpenApi3 model = loadSwagger("src/test/resources/swaggers/petstore.yaml");
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(model.getPath("/pets").getGet());
    router.get("/pets").handler(validationHandler);
    router.get("/pets").handler(routingContext -> {
      routingContext.response().setStatusMessage("ok")
        .end();
    }).failureHandler(generateFailureHandler());
    testRequest(HttpMethod.GET, "/pets", 200, "ok");
  }

}
