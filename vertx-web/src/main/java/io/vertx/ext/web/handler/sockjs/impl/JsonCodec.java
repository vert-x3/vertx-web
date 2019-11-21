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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.EncodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

  private final static ObjectMapper mapper;
  private final static JsonFactory factory = new JsonFactory();

  static {
    mapper = new ObjectMapper();

    // By default, Jackson does not escape unicode characters in JSON strings
    // This should be ok, since a valid JSON string can contain unescaped JSON
    // characters.
    // However, SockJS requires that many unicode chars are escaped. This may
    // be due to browsers barfing over certain unescaped characters
    // So... when encoding strings we make sure all unicode chars are escaped

    // This code adapted from http://wiki.fasterxml.com/JacksonSampleQuoteChars
    SimpleModule simpleModule = new SimpleModule();

    simpleModule.addSerializer(String.class, new JsonSerializer<String>() {
      final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
      final int[] ESCAPE_CODES = CharTypes.get7BitOutputEscapes();

      private void writeUnicodeEscape(JsonGenerator gen, char c) throws IOException {
        gen.writeRaw('\\');
        gen.writeRaw('u');
        gen.writeRaw(HEX_CHARS[(c >> 12) & 0xF]);
        gen.writeRaw(HEX_CHARS[(c >> 8) & 0xF]);
        gen.writeRaw(HEX_CHARS[(c >> 4) & 0xF]);
        gen.writeRaw(HEX_CHARS[c & 0xF]);
      }

      private void writeShortEscape(JsonGenerator gen, char c) throws IOException {
        gen.writeRaw('\\');
        gen.writeRaw(c);
      }

      @Override
      public void serialize(String str, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int status = ((JsonWriteContext) gen.getOutputContext()).writeValue();
        switch (status) {
          case JsonWriteContext.STATUS_OK_AFTER_COLON:
            gen.writeRaw(':');
            break;
          case JsonWriteContext.STATUS_OK_AFTER_COMMA:
            gen.writeRaw(',');
            break;
          case JsonWriteContext.STATUS_EXPECT_NAME:
            throw new JsonGenerationException("Can not write string value here", gen);
        }
        gen.writeRaw('"');
        for (char c : str.toCharArray()) {
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
    });
    mapper.registerModule(simpleModule);
  }

  public static String encode(Object obj) throws EncodeException {
    try {
      return mapper.writeValueAsString(obj);
    }
    catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON");
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
