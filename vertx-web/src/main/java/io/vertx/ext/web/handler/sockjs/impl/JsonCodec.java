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

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.sockjs.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.CharTypes;
import io.vertx.core.json.EncodeException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * SockJS requires a special JSON codec - it requires that many other characters,
 * over and above what is required by the JSON spec are escaped.
 * To satisfy this we escape any character that escapable with short escapes and
 * any other non ASCII character we unicode escape it
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class JsonCodec {

  private static final JsonFactory factory = new JsonFactory();

  // By default, Jackson does not escape unicode characters in JSON strings
  // This should be ok, since a valid JSON string can contain unescaped JSON
  // characters.
  // However, SockJS requires that many unicode chars are escaped. This may
  // be due to browsers barfing over certain unescaped characters
  // So... when encoding strings we make sure all unicode chars are escaped

  // This code was adapted from http://wiki.fasterxml.com/JacksonSampleQuoteChars

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
  private static final int[] ESCAPE_CODES = CharTypes.get7BitOutputEscapes();

  private static void writeUnicodeEscape(JsonGenerator gen, char c) throws IOException {
    gen.writeRaw('\\');
    gen.writeRaw('u');
    gen.writeRaw(HEX_CHARS[(c >> 12) & 0xF]);
    gen.writeRaw(HEX_CHARS[(c >> 8) & 0xF]);
    gen.writeRaw(HEX_CHARS[(c >> 4) & 0xF]);
    gen.writeRaw(HEX_CHARS[c & 0xF]);
  }

  private static void writeShortEscape(JsonGenerator gen, char c) throws IOException {
    gen.writeRaw('\\');
    gen.writeRaw(c);
  }

  public static String encode(String[] messages) throws EncodeException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator gen = factory.createGenerator(sw)) {
      gen.writeStartArray();
      boolean first = true;
      for (String message : messages) {
        if (first) {
          first = false;
        } else {
          gen.writeRaw(',');
        }
        gen.writeRaw('"');
        for (char c : message.toCharArray()) {
          if (c >= 0x80) writeUnicodeEscape(gen, c); // use generic escaping for all non US-ASCII characters
          else {
            // use escape table for first 128 characters
            int code = (c < ESCAPE_CODES.length ? ESCAPE_CODES[c] : 0);
            if (code == 0) gen.writeRaw(c); // no escaping
            else if (code == -1) writeUnicodeEscape(gen, c); // generic escaping
            else writeShortEscape(gen, (char) code); // short escaping (\n \t ...)
          }
        }
        gen.writeRaw('"');
      }
      gen.writeEndArray();
      gen.close();
      return sw.toString();
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON", e);
    }
  }

  public static List<String> decodeValues(String messages) {
    List<String> result = null;
    try (JsonParser parser = factory.createParser(messages)) {
      JsonToken jsonToken = parser.nextToken();
      if (jsonToken == JsonToken.START_ARRAY) {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
          if (result == null) {
            result = new ArrayList<>();
          }
          result.add(parser.getValueAsString());
        }
      } else if (jsonToken == JsonToken.VALUE_STRING) {
        result = Collections.singletonList(parser.getValueAsString());
      }
      return result != null ? result : Collections.emptyList();
    } catch (Exception ignore) {
      return null;
    }
  }
}
