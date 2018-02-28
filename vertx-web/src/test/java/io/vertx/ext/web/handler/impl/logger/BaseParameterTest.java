package io.vertx.ext.web.handler.impl.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class BaseParameterTest {

  @Mock
  RoutingContext ctx;
  @Mock
  HttpServerRequest req;
  @Mock
  HttpServerResponse resp;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void before() {
    when(ctx.response()).thenReturn(resp);
    when(ctx.request()).thenReturn(req);
  }

  @Test
  public void testBaseParameter_modifiersRequiredFor400AndResponse400() {
    modifiersTestHelper("vertx-web", "vertx-web", 400, new int[]{400}, true);
  }

  @Test
  public void testBaseParameter_modifiersRequiredFor400AndResponse200() {
    modifiersTestHelper("-", "vertx-web", 200, new int[]{400}, true);
  }

  @Test
  public void testBaseParameter_modifiersRequiredFor300and400AndResponse400() {
    modifiersTestHelper("vertx-web", "vertx-web", 400, new int[]{300, 400}, true);
  }

  @Test
  public void testBaseParameter_modifiersRequiredFor300and400AndResponse200() {
    modifiersTestHelper("-", "vertx-web", 200, new int[]{300, 400}, true);
  }

  @Test
  public void testBaseParameter_modifiersNotRequiredFor300and400AndResponse200() {
    modifiersTestHelper("vertx-web", "vertx-web", 200, new int[]{300, 400}, false);
  }

  @Test
  public void testBaseParameter_modifiersNotRequiredFor300and400AndResponse300() {
    modifiersTestHelper("-", "vertx-web", 300, new int[]{300, 400}, false);
  }

  @Test
  public void testBaseParameter_escape() {
    // single whitespace character
    assertThat(BaseParameter.escape("\n"), equalTo("\\n"));
    assertThat(BaseParameter.escape("\r"), equalTo("\\r"));
    assertThat(BaseParameter.escape("\t"), equalTo("\\t"));
    assertThat(BaseParameter.escape("\f"), equalTo("\\f"));
    assertThat(BaseParameter.escape("\b"), equalTo("\\b"));

    // single special character
    assertThat(BaseParameter.escape("\\"), equalTo("\\\\"));
    assertThat(BaseParameter.escape("\""), equalTo("\\\""));

    // plain text
    assertThat(BaseParameter.escape("This is a plain text"), equalTo("This is a plain text"));

    // embedded whitespace special
    assertThat(BaseParameter.escape("This\b is\f a\nplain word"),
      equalTo("This\\b is\\f a\\nplain word"));

    // embedded non-printable
    assertThat(BaseParameter.escape("Das isch \u00e4n Umlaut"),
      equalTo("Das isch \\u00e4n Umlaut"));
    assertThat(BaseParameter.escape("This is a very special character \u1234"),
      equalTo("This is a very special character \\u1234"));
  }

  private void modifiersTestHelper(String expectedHeader, String responseHeader,
    int responseStatus, int[] statusLimits, boolean required) {
    when(resp.getStatusCode()).thenReturn(responseStatus);
    when(req.headers())
      .thenReturn(MultiMap.caseInsensitiveMultiMap().add("user-agent", responseHeader));

    HeaderParameter test = new HeaderParameter("User-Agent", true);
    test.setRequired(required);
    test.setStatusLimits(statusLimits);

    assertThat(test.print(ctx).toString(), equalTo(expectedHeader));
  }
}
