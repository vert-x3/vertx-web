package io.vertx.ext.web.client.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;

/**
 * @author <a href="mailto:pabloeabad@gmail.com">Pablo Abad</a>
 */
class ParametrizedUri {
  private static final Pattern PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");

  private boolean simple;
  private String raw;
  private List<String> rawParts;
  private Map<String, Integer> paramIndexes;

  public ParametrizedUri(String uri) {
    this.raw = uri;
    extractParts();
  }

  public ParametrizedUri(ParametrizedUri uri) {
    this.raw = uri.raw;
    this.simple = uri.simple;
    this.rawParts = uri.rawParts == null ? null : new ArrayList<>(uri.rawParts);
    this.paramIndexes = uri.paramIndexes == null ? null : new HashMap<>(uri.paramIndexes);
  }

  private void extractParts() {
    Matcher m =  PLACEHOLDER.matcher(raw);
    int index = 0;
    while (m.find()) {
      if (index == 0) {
        rawParts = new ArrayList<>();
        paramIndexes = new HashMap<>();
      }

      String name = m.group(1);
      if (paramIndexes.containsKey(name)) {
        throw new IllegalArgumentException("Cannot use identifier " + name + " more than once in pattern string");
      }

      // Add verbatim text before placeholder for value
      rawParts.add(raw.substring(index, m.start()));
      rawParts.add(null);

      paramIndexes.put(name, rawParts.size() - 1);
      index = m.end();
    }
    if (index == 0) {
      simple = true;
      return;
    }
    if (index < raw.length() - 1) {
      rawParts.add(raw.substring(index));
    }
  }

  public ParametrizedUri setParam(String name, String value) {
    int pos = indexFor(name);
    rawParts.set(pos, pathSegmentEscape(value));
    return this;
  }

  public ParametrizedUri setParam(String name, long value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value for parameter " + name + " can't be negative. Value: " +  value);
    }
    int pos = indexFor(name);
    rawParts.set(pos, String.valueOf(value));
    return this;
  }

  private int indexFor(String name) {
    Integer pos = paramIndexes == null ? null : paramIndexes.get(name);
    if (pos == null) {
      throw new IllegalArgumentException("Path parameter " + name + " is not part of the uri pattern.");
    }
    return pos;
  }

  private String pathSegmentEscape(String value) {
    try {
      URI uri = new URI(null, null, value, null);
      String escaped = uri.getRawPath();
      // Escape slashes to prevent injecting other path segments
      if (escaped.contains("/")) {
        escaped = escaped.replaceAll("/", "%2f");
      }
      return escaped;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Can't encode " + value + " as a path parameter");
    }
  }

  @Override
  public String toString() {
    if (simple) {
      return raw;
    }

    StringBuilder sb = new StringBuilder();
    for (String part : rawParts) {
      if (part == null) {
        throw new IllegalStateException("Missing path parameters: " + missingParams());
      }
      sb.append(part);
    }
    return sb.toString();
  }

  private String missingParams() {
    return paramIndexes.entrySet().stream()
      .filter(e -> rawParts.get(e.getValue()) == null)
      .map(e -> e.getKey())
      .collect(joining(", "));
  }

  public String raw() {
    return raw;
  }
}
