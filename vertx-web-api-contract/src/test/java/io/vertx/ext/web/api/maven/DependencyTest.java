package io.vertx.ext.web.api.maven;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class DependencyTest {

  @Test
  public void testNoGuavaDependency () throws Exception {
    assertNull (getClass ().getResource ("/META-INF/maven/com.google.guava/guava/pom.properties"));
  }

  @Test
  public void testNoFirebugDependency () throws Exception {
    assertNull (getClass ().getResource ("/META-INF/maven/com.google.code.findbugs/jsr305"));
  }

}
