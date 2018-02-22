package io.vertx.ext.web.handler.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.vertx.ext.web.RoutingContext;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(Parameterized.class)
public class TimeParameterTest {

  @Mock
  RoutingContext ctx;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
      new Object[][]{
        {"%a", "Thu"},
        {"%A", "Thursday"},
        {"%b", "Feb"},
        {"%B", "February"},
        {"%c", "Thu Feb 22 16:44:13 2018"},
        {"%d", "22"},
        {"%D", "02/22/18"},
        {"%e", "22"},
        {"%F", "2018-02-22"},
        {"%g", "18"},
        {"%G", "2018"},
        {"%h", "Feb"},
        {"%H", "16"},
        {"%I", "04"},
        {"%j", "053"},
        {"%k", "16"},
        {"%l", "4"},
        {"%m", "02"},
        {"%M", "44"},
        {"%n", "\n"},
        {"%p", "PM"},
        {"%P", "PM"},
        {"%r", "04:44:13 PM"},
        {"%R", "16:44"},
        {"%S", "13"},
        {"%t", "\t"},
        {"%T", "16:44:13"},
        {"%V", "08"},
        {"%x", "02/22/18"},
        {"%X", "16:44:13"},
        {"%y", "18"},
        {"%Y", "2018"},
        {"%z", "+0100"},
        {"%Z", "CET"},
        {"%u", "4"},
        {"%%", "%"},
        {"%s", "%s"} // Not supported
      }
    );
  }

  @Parameter
  public String strfFormat;

  @Parameter(1)
  public String expected;

  @Test
  public void testTimeParameter_default() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("22/Feb/2018:14:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter(null, null);
    assertThat(test.getValue(ctx, false), equalTo("22/Feb/2018:14:44:13 +0100"));
  }

  @Test
  public void testTimeParameter_escapePercent() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("22/Feb/2018:14:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter("%d/%b/%Y:%k:%M:%S%% %z", null);
    assertThat(test.getValue(ctx, false), equalTo("22/Feb/2018:14:44:13% +0100"));
  }

  @Test
  public void testTimeParameters() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("22/Feb/2018:16:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter(strfFormat, null);
    assertThat(test.getValue(ctx, false), equalTo(expected));
  }

  @Test
  public void testTimeParameter_eParam() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("02/Feb/2018:16:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter("%e", null);
    assertThat(test.getValue(ctx, false), equalTo("2"));
  }

  @Test
  public void testTimeParameter_kParam() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("02/Feb/2018:05:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter("%k", null);
    assertThat(test.getValue(ctx, false), equalTo("5"));
  }

  @Test
  public void testTimeParameter_lParam() throws Exception {
    when(ctx.get("logger-requestStart"))
      .thenReturn(getDate("02/Feb/2018:15:44:13 +0100").getTime());

    TimeParameter test = new TimeParameter("%l", null);
    assertThat(test.getValue(ctx, false), equalTo("3"));
  }


  private Date getDate(String text) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
    return sdf.parse(text);
  }
}
