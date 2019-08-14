package io.vertx.ext.web.impl;

import io.vertx.ext.web.ParsedHeaderValue;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HeaderParserTest {

  @Test
  public void testSplit() {
    try {
      HeaderParser.convertToParsedHeaderValues("big5, big5-hkscs, cesu-8, euc-jp, euc-kr, gb18030, gb2312, gbk, ibm-thai, ibm00858, ibm01140, ibm01141, ibm01142, ibm01143, ibm01144, ibm01145, ibm01146, ibm01147, ibm01148, ibm01149, ibm037, ibm1026, ibm1047, ibm273, ibm277, ibm278, ibm280, ibm284, ibm285, ibm290, ibm297, ibm420, ibm424, ibm437, ibm500, ibm775, ibm850, ibm852, ibm855, ibm857, ibm860, ibm861, ibm862, ibm863, ibm864, ibm865, ibm866, ibm868, ibm869, ibm870, ibm871, ibm918, iso-2022-cn, iso-2022-jp, iso-2022-jp-2, iso-2022-kr, iso-8859-1, iso-8859-13, iso-8859-15, iso-8859-2, iso-8859-3, iso-8859-4, iso-8859-5, iso-8859-6, iso-8859-7, iso-8859-8, iso-8859-9, jis_x0201, jis_x0212-1990, koi8-r, koi8-u, shift_jis, tis-620, us-ascii, utf-16, utf-16be, utf-16le, utf-32, utf-32be, utf-32le, utf-8, windows-1250, windows-1251, windows-1252, windows-1253, windows-1254, windows-1255, windows-1256, windows-1257, windows-1258, windows-31j, x-big5-hkscs-2001, x-big5-solaris, x-compound_text, x-euc-jp-linux, x-euc-tw, x-eucjp-open, x-ibm1006, x-ibm1025, x-ibm1046, x-ibm1097, x-ibm1098, x-ibm1112, x-ibm1122, x-ibm1123, x-ibm1124, x-ibm1166, x-ibm1364, x-ibm1381, x-ibm1383, x-ibm300, x-ibm33722, x-ibm737, x-ibm833, x-ibm834, x-ibm856, x-ibm874, x-ibm875, x-ibm921, x-ibm922, x-ibm930, x-ibm933, x-ibm935, x-ibm937, x-ibm939, x-ibm942, x-ibm942c, x-ibm943, x-ibm943c, x-ibm948, x-ibm949, x-ibm949c, x-ibm950, x-ibm964, x-ibm970, x-iscii91, x-iso-2022-cn-cns, x-iso-2022-cn-gb, x-iso-8859-11, x-jis0208, x-jisautodetect, x-johab, x-macarabic, x-maccentraleurope, x-maccroatian, x-maccyrillic, x-macdingbat, x-macgreek, x-machebrew, x-maciceland, x-macroman, x-macromania, x-macsymbol, x-macthai, x-macturkish, x-macukraine, x-ms932_0213, x-ms950-hkscs, x-ms950-hkscs-xp, x-mswin-936, x-pck, x-sjis_0213, x-utf-16le-bom, x-utf-32be-bom, x-utf-32le-bom, x-windows-50220, x-windows-50221, x-windows-874, x-windows-949, x-windows-950, x-windows-iso2022jp", ParsableHeaderValue::new);
    } catch (StackOverflowError e) {
      fail(e.getMessage());
    }

    // test all forms
    List<ParsedHeaderValue> headers = HeaderParser.convertToParsedHeaderValues("text/html,text/plain;q=0.9,application/json;q=0.8", ParsableHeaderValue::new);

    assertEquals(3, headers.size());
    assertTrue(headers.contains(new ParsableHeaderValue("text/html")));
    assertTrue(headers.contains(new ParsableHeaderValue("text/plain;q=0.9")));
    assertTrue(headers.contains(new ParsableHeaderValue("application/json;q=0.8")));

    // test parameters and escaping
    headers = HeaderParser.convertToParsedHeaderValues("text/html;boo=\"yeah\\\",right\";itWorks=4real;bar=\"some text\"", ParsableHeaderValue::new);

    assertEquals(1, headers.size());
    assertTrue(headers.contains(new ParsableHeaderValue("text/html;boo=\"yeah\\\",right\";itWorks=4real;bar=\"some text\"")));

    ((ParsableHeaderValue) headers.get(0)).forceParse();

    assertTrue(headers.get(0).parameters().containsKey("boo"));
    assertTrue(headers.get(0).parameters().containsKey("itWorks"));
    assertTrue(headers.get(0).parameters().containsKey("bar"));
    // validate that the unquote worked
    assertEquals("yeah\",right", headers.get(0).parameters().get("boo"));
    assertEquals("some text", headers.get(0).parameters().get("bar"));

    // test extra white space
    headers = HeaderParser.convertToParsedHeaderValues("big5 , big5-hkscs , cesu-8 , ", ParsableHeaderValue::new);
    assertEquals(3, headers.size());
  }

}
