package io.vertx.ext.web.impl;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.ParsedHeaderValue;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Build with the intent of following
 * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.1">rfc7231,section-5.3.1</a>'s specification.<br>
 */
public class HeaderParser {
  private static final Logger log = LoggerFactory.getLogger(HeaderParser.class);

  private static final Comparator<ParsedHeaderValue> HEADER_SORTER =
    (ParsedHeaderValue left, ParsedHeaderValue right) -> right.weightedOrder() - left.weightedOrder();

  /**
   * Transforms each header value into the given ParsableHeaderValue
   *
   * @param unparsedHeaderValue The header to split
   * @param objectCreator       The type to instantiate for each header
   * @return The list of (unparsed) parsable header value
   */
  public static <T extends ParsedHeaderValue> List<T> convertToParsedHeaderValues(String unparsedHeaderValue, Function<String, T> objectCreator) {
    return split(unparsedHeaderValue, ',', objectCreator);
  }

  /**
   * In-place sorting of the headers list
   *
   * @param headers
   * @return The same object as inserted
   */
  public static <T extends ParsedHeaderValue> List<T> sort(List<T> headers) {
    headers.sort(HEADER_SORTER);
    return headers;
  }

  /**
   * Parses a header value
   *
   * @param headerContent
   * @param valueCallback
   * @param weightCallback
   * @param parameterCallback
   */
  public static void parseHeaderValue(String headerContent, Consumer<String> valueCallback, Consumer<Float> weightCallback, BiConsumer<String, String> parameterCallback) {

    int paramIndex = headerContent.indexOf(';');

    if (paramIndex < 0) {
      valueCallback.accept(headerContent);
    } else {
      // the whole value
      valueCallback.accept(headerContent.substring(0, paramIndex));

      if (paramIndex < headerContent.length()) {

        split(headerContent.substring(paramIndex + 1), ';', part -> {
          int idx = part.indexOf('=');
          if (idx != -1) {
            final String key = part.substring(0, idx);
            final String val = part.substring(idx + 1);

            if ("q".equalsIgnoreCase(key)) {
              try {
                weightCallback.accept(Float.parseFloat(val));
              } catch (NumberFormatException e) {
                log.info("Found a \"q\" parameter with value \"{}\" which was unparsable", val);
              }
            } else {
              parameterCallback.accept(key, unquote(val));
            }
          } else {
            // no value associated with this key
            parameterCallback.accept(part, null);
          }

          return null;
        });
      }
    }
  }

  public static void parseMIME(
    String headerContent,
    Consumer<String> componentCallback,
    Consumer<String> subcomponentCallback
  ) {

    int slashIndex = headerContent.indexOf('/');
    int paramIndex = headerContent.indexOf(';', slashIndex + 1);

    if (slashIndex < 0) {
      componentCallback.accept("*");
    } else {
      componentCallback.accept(headerContent.substring(0, slashIndex).toLowerCase());
    }

    if (paramIndex < 0) {
      subcomponentCallback.accept(headerContent.substring(slashIndex + 1));
    } else {
      subcomponentCallback.accept(headerContent.substring(slashIndex + 1, paramIndex).toLowerCase());
    }
  }


  public static List<String> parseLanguageValue(String value) {
    if (value == null || value.length() == 0) {
      return Collections.emptyList();
    }

    final List<String> parts = new LinkedList<>();

    // state machine
    int start = 0;

    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      // trim initial white space
      if (start == i && ch == ' ') {
        start++;
        continue;
      }

      // splitting logic uses 2 chars, since java Locales use underscore
      if (ch == '-' || ch == '_') {
        int end = i;
        // trim end white space
        for (int j = i - 1; j >= start; j--) {
          if (value.charAt(j) == ' ') {
            end--;
            continue;
          }
          break;
        }
        // ignore empty
        if (end - start > 0) {
          parts.add(value.substring(start, end));
          if (parts.size() == 3) {
            // force stop, we have country, language and variant
            return parts;
          }
        }
        start = i + 1;
      }
    }

    // rest
    if (start < value.length()) {
      int end = value.length();
      // trim end white space
      for (int j = value.length() - 1; j >= start; j--) {
        if (value.charAt(j) == ' ') {
          end--;
          continue;
        }
        break;
      }
      // ignore empty
      if (end - start > 0) {
        parts.add(value.substring(start, end));
      }
    }

    return parts;
  }

  private static <T> List<T> split(String header, char split, Function<String, T> factory) {
    if (header == null || header.length() == 0) {
      return Collections.emptyList();
    }

    final List<T> parts = new LinkedList<>();

    // state machine
    boolean quote = false;
    int start = 0;
    char last = 0;

    for (int i = 0; i < header.length(); i++) {
      char ch = header.charAt(i);
      // trim initial white space
      if (start == i && ch == ' ') {
        start++;
        continue;
      }
      // identify if we're handling quoted strings
      if (ch == '\"' && last != '\\') {
        quote = !quote;
      }

      last = ch;
      // splitting logic only applies outside quoted strings
      if (!quote && ch == split) {
        int end = i;
        // trim end white space
        for (int j = i - 1; j >= start; j--) {
          if (header.charAt(j) == ' ') {
            end--;
            continue;
          }
          break;
        }
        // ignore empty
        if (end - start > 0) {
          parts.add(factory.apply(header.substring(start, end)));
        }
        start = i + 1;
      }
    }

    // rest
    if (start < header.length()) {
      int end = header.length();
      // trim end white space
      for (int j = header.length() - 1; j >= start; j--) {
        if (header.charAt(j) == ' ') {
          end--;
          continue;
        }
        break;
      }
      // ignore empty
      if (end - start > 0) {
        parts.add(factory.apply(header.substring(start, end)));
      }
    }

    return parts;
  }

  private static String unquote(String value) {
    if (value == null || value.length() == 0) {
      return value;
    }

    StringBuilder sb = null;

    int start = 0;
    int end = value.length();

    // adjust start if there is a quote
    if (value.charAt(start) == '\"') {
      start++;
    }

    // adjust end if there is a quote
    if (value.charAt(end - 1) == '\"') {
      end--;
    }

    // look for extra quotes in the value itself
    for (int i = start ; i < end; i++) {
      if (value.charAt(i) == '\\') {
        if (sb == null) {
          sb = new StringBuilder(value.substring(start, i));
        }
        continue;
      }
      if (sb != null) {
        sb.append(value.charAt(i));
      }
    }

    if (sb != null) {
      return sb.toString();
    } else {
      // the value is quoted
      if (end - start != value.length()) {
        return value.substring(start, end);
      }
      return value;
    }
  }
}
