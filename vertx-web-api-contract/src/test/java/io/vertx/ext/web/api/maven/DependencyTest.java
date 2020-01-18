package io.vertx.ext.web.api.maven;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DependencyTest {

  @Test
  public void testGuavaDependency () throws Exception {
    assertNotNull (getClass ().getResource ("/META-INF/maven/com.google.guava/guava/pom.properties"));
  }

  @Test
  public void testFirebugDependency () throws Exception {
    assertNotNull (getClass ().getResource ("/META-INF/maven/com.google.code.findbugs/jsr305"));
  }

}
