/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.json.EncodeException;
import io.vertx.ext.web.handler.sockjs.impl.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonCodecTest {

  // --- encode() tests ---

  @Test
  public void encodeEmptyArray() {
    assertEquals("[]", JsonCodec.encode(new String[]{}));
  }

  @Test
  public void encodeSingleMessage() {
    String msg = "{\"type\":\"send\",\"address\":\"test\",\"body\":\"hello\"}";
    String encoded = JsonCodec.encode(new String[]{msg});
    assertEquals("[\"" +
      "{\\\"type\\\":\\\"send\\\",\\\"address\\\":\\\"test\\\",\\\"body\\\":\\\"hello\\\"}" +
      "\"]", encoded);
  }

  @Test
  public void encodeMultipleMessages() {
    String encoded = JsonCodec.encode(new String[]{"a", "b", "c"});
    assertEquals("[\"a\",\"b\",\"c\"]", encoded);
  }

  @Test
  public void encodeEmptyStringMessage() {
    assertEquals("[\"\"]", JsonCodec.encode(new String[]{""}));
  }

  @Test
  public void encodeQuoteAndBackslash() {
    assertEquals("[\"say \\\"hi\\\"\"]", JsonCodec.encode(new String[]{"say \"hi\""}));
    assertEquals("[\"back\\\\slash\"]", JsonCodec.encode(new String[]{"back\\slash"}));
  }

  @Test
  public void encodeShortEscapes() {
    assertEquals("[\"a\\nb\"]", JsonCodec.encode(new String[]{"a\nb"}));
    assertEquals("[\"a\\tb\"]", JsonCodec.encode(new String[]{"a\tb"}));
    assertEquals("[\"a\\rb\"]", JsonCodec.encode(new String[]{"a\rb"}));
    assertEquals("[\"a\\bb\"]", JsonCodec.encode(new String[]{"a\bb"}));
    assertEquals("[\"a\\fb\"]", JsonCodec.encode(new String[]{"a\fb"}));
  }

  @Test
  public void encodeControlCharsUnicodeEscaped() {
    assertEquals("[\"\\u0000\"]", JsonCodec.encode(new String[]{"\u0000"}));
    assertEquals("[\"\\u0001\"]", JsonCodec.encode(new String[]{"\u0001"}));
    assertEquals("[\"\\u001f\"]", JsonCodec.encode(new String[]{"\u001f"}));
  }

  @Test
  public void encodeControlToNormalBoundary() {
    // 0x1F is the last control char (unicode-escaped), 0x20 (space) is not escaped
    assertEquals("[\"\\u001f \"]", JsonCodec.encode(new String[]{"\u001f "}));
  }

  @Test
  public void encodeAsciiToNonAsciiBoundary() {
    // 0x7F (DEL) is not escaped (matches Jackson), 0x80 is the first unicode-escaped char
    assertEquals("[\"" + (char) 0x7F + "\\u0080\"]", JsonCodec.encode(new String[]{"\u007f\u0080"}));
  }

  @Test
  public void encodeNonAsciiUnicodeEscaped() {
    // SockJS requires all non-ASCII characters to be unicode-escaped
    assertEquals("[\"\\u00e9\"]", JsonCodec.encode(new String[]{"\u00e9"}));   // e-acute
    assertEquals("[\"\\u4e2d\"]", JsonCodec.encode(new String[]{"\u4e2d"}));   // CJK char
  }

  @Test
  public void encodeLineSeparatorsEscaped() {
    // U+2028 and U+2029 break JS eval() — must be escaped for SockJS safety
    assertEquals("[\"\\u2028\\u2029\"]", JsonCodec.encode(new String[]{"\u2028\u2029"}));
  }

  @Test
  public void encodeSurrogatePairs() {
    // U+1F600 (grinning face) is represented as surrogate pair D83D DE00
    String emoji = "\uD83D\uDE00";
    assertEquals("[\"\\ud83d\\ude00\"]", JsonCodec.encode(new String[]{emoji}));
  }

  @Test
  public void encodeMixedContent() {
    // Realistic event bus message body with mixed escaping needs
    String msg = "{\"body\":\"caf\u00e9\n\"quoted\"\"}";
    String encoded = JsonCodec.encode(new String[]{msg});
    assertEquals("[\"" +
      "{\\\"body\\\":\\\"caf\\u00e9\\n\\\"quoted\\\"\\\"}" +
      "\"]", encoded);
  }

  @Test
  public void encodeNullElementThrows() {
    assertThrows(EncodeException.class, () -> JsonCodec.encode(new String[]{null}));
  }

  // --- decodeValues() tests ---

  @Test
  public void decodeNull() {
    assertEquals(Collections.emptyList(), JsonCodec.decodeValues(null));
  }

  @Test
  public void decodeEmptyString() {
    assertEquals(Collections.emptyList(), JsonCodec.decodeValues(""));
  }

  @Test
  public void decodeJsonArray() {
    List<String> result = JsonCodec.decodeValues("[\"msg1\",\"msg2\"]");
    assertEquals(List.of("msg1", "msg2"), result);
  }

  @Test
  public void decodeSingleJsonString() {
    List<String> result = JsonCodec.decodeValues("\"hello\"");
    assertEquals(List.of("hello"), result);
  }

  @Test
  public void decodeEmptyJsonArray() {
    assertEquals(Collections.emptyList(), JsonCodec.decodeValues("[]"));
  }

  @Test
  public void decodeMalformedJson() {
    assertNull(JsonCodec.decodeValues("{broken"));
  }

  @Test
  public void decodeJsonObject() {
    assertEquals(Collections.emptyList(), JsonCodec.decodeValues("{\"key\":\"val\"}"));
  }

  @Test
  public void decodeJsonNumber() {
    assertEquals(Collections.emptyList(), JsonCodec.decodeValues("42"));
  }

  @Test
  public void decodeWhitespaceOnly() {
    assertNull(JsonCodec.decodeValues("   "));
  }

  @Test
  public void decodeArrayWithNonStringValues() {
    // JsonArray.getString() uses toString() for non-string values
    assertEquals(List.of("1", "true"), JsonCodec.decodeValues("[1, true]"));
  }

  // --- round-trip tests ---

  @Test
  public void roundTrip() {
    String[] messages = {
      "{\"type\":\"send\",\"address\":\"test\",\"body\":\"hello\"}",
      "{\"type\":\"publish\",\"address\":\"news\",\"body\":\"caf\u00e9\"}"
    };
    String encoded = JsonCodec.encode(messages);
    List<String> decoded = JsonCodec.decodeValues(encoded);
    assertEquals(List.of(messages), decoded);
  }

  @Test
  public void roundTripJsonInjectionAttempt() {
    // Malicious payload attempting to break out of the JSON array structure
    String malicious = "\"],\"injected\",\"";
    String encoded = JsonCodec.encode(new String[]{malicious});
    List<String> decoded = JsonCodec.decodeValues(encoded);
    assertEquals(1, decoded.size());
    assertEquals(malicious, decoded.get(0));
  }

  @Test
  public void roundTripSockJSFrame() {
    // SockJS message frames are prefixed with "a" by the transport layer
    String[] messages = {"{\"type\":\"register\",\"address\":\"chat.room1\"}"};
    String frame = "a" + JsonCodec.encode(messages);
    assertTrue(frame.startsWith("a["));
    // The transport strips the "a" prefix before calling decodeValues
    List<String> decoded = JsonCodec.decodeValues(frame.substring(1));
    assertEquals(List.of(messages), decoded);
  }
}
