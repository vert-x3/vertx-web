package io.vertx.ext.web.client.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.vertx.ext.web.client.CookieStore;

public class TreeCookieStore implements InternalCookieStore{

  private Node root;
  
  public TreeCookieStore() {
    root = new Node(null, 0, "");
  }

  @Override
  public CookieStore put(String name, String value) {
    return put(name, value, null, null, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String path) {
    return put(name, value, null, path, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String domain, String path) {
    return put(name, value, domain, path, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String domain, String path, Long maxAge, boolean isSecure) {
    DefaultCookie cookie = new DefaultCookie(name, value);
    cookie.setSecure(isSecure);
    if (domain != null)
      cookie.setDomain(domain);
    if (path != null)
      cookie.setPath(path);
    if (maxAge != null)
      cookie.setMaxAge(maxAge);
    return put(cookie);
  }

  @Override
  public CookieStore put(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    Node node = root.findPrefixNode(key.domain);
    if (node.depth == key.domain.length - 1) {
      // Exact match
      node.addCookie(cookie);
      return this;
    }
    
    // Create missing nodes
    for (int i = node.depth + 1; i < key.domain.length; i++) {
      node = node.createChild(key.domain[i]);
    }
    node.addCookie(cookie);
    return this;
  }

  @Override
  public CookieStore remove(String name) {
    remove(name, null, null);
    return this;
  }

  @Override
  public CookieStore remove(String name, String path) {
    remove(name, null, path);
    return this;
  }

  @Override
  public CookieStore remove(String name, String domain, String path) {
    Key key = new Key(domain, path, name);
    Node node = root.findPrefixNode(key.domain);
    if (node.depth != key.domain.length - 1) {
      // Domain not found
      return this;
    }
    node.remove(path, name);
    return this;
  }
  
  @Override
  public Iterable<Cookie> get(boolean ssl, String domain, String path) {
    /* TODO: Shall we cleanup path (remove dots)? Or HttpRequestImpl.uri is always normalized?
     *       Ideally this should call io.vertx.ext.web.impl.Utils.removeDots
     *       but vertx-web-client does not have a dependency on vertx-web so, probably, the Utils class (or parts of it)
     *       should be moved to vertx-web-commons.
     */

    Key key = new Key(domain, null, null);
    TreeMap<String, Cookie> matches = new TreeMap<>();
    root.visit(key.domain, n -> {
      for (Cookie c : n.getCookies()) {
        if (!ssl && c.isSecure()) {
          continue;
        }
        if (c.path() != null && !path.equals(c.path())) {
          String cookiePath = c.path();
          if (!cookiePath.endsWith("/")) {
            cookiePath += '/';
          }
          if (!path.startsWith(cookiePath)) {
            continue;
          }
        }
        matches.put(c.name(), c);
      }
    });
    return matches.values();
  }

  private static class Node implements Comparable<Node> {

    private static final String NO_PATH = " ";

    @SuppressWarnings("unused")
    private Node parent;
    private int depth;
    private String name;
    private TreeSet<Node> children;
    private TreeMap<String, Cookie> cookies;

    public Node(Node parent, int depth, String name) {
      Objects.requireNonNull(name);
      this.parent = parent;
      this.depth = depth;
      this.name = name;
      this.children = new TreeSet<>();
      this.cookies = new TreeMap<>();
    }

    public Node createChild(String name) {
      Node c = new Node(this, depth + 1, name);
      children.add(c);
      return c;
    }

    private String buildCookieKey(String path, String name) {
      StringBuilder builder = new StringBuilder();
      builder.append(path == null || path.length() == 0 ? NO_PATH : path).append(':').append(name);
      return builder.toString();
    }

    public void addCookie(Cookie cookie) {
      String k = buildCookieKey(cookie.path(), cookie.name());
      cookies.put(k, cookie);
    }

    public boolean remove(String path, String name) {
      String k = buildCookieKey(path, name);
      return cookies.remove(k) != null;
    }

    private Node findPrefixNode(String[] domain) {
      String segment = domain[depth];
      assert segment.equals(this.name) : "Expected [" + this.name + "] got [" + segment + "]";
      
      if (depth == domain.length -1) {
        return this;
      }
      
      segment = domain[depth + 1];
      for (Node node : children) {
        if (node.name.equals(segment)) {
          return node.findPrefixNode(domain);
        } else if (node.name.compareTo(segment) > 0) {
          break;
        }
      }
      
      return this;
    }

    public void visit(String[] domain, Consumer<Node> visitor) {
      String segment = domain[depth];
      assert segment.equals(this.name) : "Expected [" + this.name + "] got [" + segment + "]";
      
      visitor.accept(this);
      if (depth == domain.length - 1) {
        return;
      }
      
      segment = domain[depth + 1];
      for (Node node : children) {
        if (node.name.equals(segment)) {
          node.visit(domain, visitor);
          return;
        } else if (node.name.compareTo(segment) > 0) {
          return;
        }
      }
    }
    
    public Collection<Cookie> getCookies() {
      return cookies.values();
    }

    @Override
    public int compareTo(Node o) {
      return name.compareTo(o.name);
    }
  }

  private static class Key implements Comparable<Key> {
    private static final String[] NO_DOMAIN = new String[] { "" };
    
    private final String[] domain;
    private final String path;
    private final String name;

    public Key(String domain, String path, String name) {
      if (domain == null || domain.length() == 0) {
        this.domain = NO_DOMAIN;
      } else {
        while (domain.charAt(0) == '.') {
          domain = domain.substring(1);
        }
        while (domain.charAt(domain.length() - 1) == '.') {
          domain = domain.substring(0, domain.length() - 1);
        }
        if (domain.length() == 0) {
          this.domain = NO_DOMAIN;
        } else {
          String[] tokens = domain.split("\\.");
          String tmp;
          for (int i = 0, j = tokens.length - 1; i < tokens.length / 2; ++i, --j) {
            tmp = tokens[j];
            tokens[j] = tokens[i];
            tokens[i] = tmp;
          }
          this.domain = new String[tokens.length + 1];
          this.domain[0] = "";
          System.arraycopy(tokens, 0, this.domain, 1, tokens.length);
        }
      }      
      this.path = path == null ? "" : path;
      this.name = name;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(domain);
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Key other = (Key) obj;
      if (!Arrays.equals(domain, other.domain))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      return true;
    }

    @Override
    public int compareTo(Key o) {
      int ret = 0;
      for (int i = 0; i < Math.min(domain.length, o.domain.length); ++i) {
        ret = domain[i].compareTo(o.domain[i]);
        if (ret != 0) {
          break;
        }
      }
      if (ret == 0)
        ret = domain.length - o.domain.length;
      if (ret == 0)
        ret = path.compareTo(o.path);
      if (ret == 0)
        ret = name.compareTo(o.name);
      return ret;
    }
  }
  
}
