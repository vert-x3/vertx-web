package io.vertx.ext.web;

import org.junit.Test;

import java.net.URI;

public class TrashTest {

  @Test
  public void test() throws Exception {
    URI u = new URI("file:///D:/a/vertx-web/vertx-web/vertx-web-openapi/src/test/resources/yaml/valid/refs/Circular.yaml");
    System.out.println(u.getPath());
  }
}
