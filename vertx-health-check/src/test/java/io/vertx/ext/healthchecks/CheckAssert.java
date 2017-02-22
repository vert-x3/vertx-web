package io.vertx.ext.healthchecks;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.assertj.core.api.AbstractAssert;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CheckAssert extends AbstractAssert<CheckAssert, JsonObject> {

  private CheckAssert parent;

  public CheckAssert(JsonObject actual) {
    super(actual, CheckAssert.class);
  }

  public CheckAssert(JsonObject actual, CheckAssert parent) {
    this(actual);
    this.parent = parent;
  }

  public CheckAssert isUp() {
    assertThat(actual).isNotNull();
    String status = actual.getString("status", actual.getString("outcome"));
    assertThat(status).isEqualTo("UP");
    return this;
  }

  public CheckAssert isDown() {
    assertThat(actual).isNotNull();
    String status = actual.getString("status", actual.getString("outcome"));
    assertThat(status).isEqualTo("DOWN");
    return this;
  }

  public CheckAssert hasStatusDown() {
    assertThat(actual).isNotNull();
    String status = actual.getString("status");
    assertThat(status).isEqualTo("DOWN");
    return this;
  }

  public CheckAssert hasStatusUp() {
    assertThat(actual).isNotNull();
    String status = actual.getString("status");
    assertThat(status).isEqualTo("UP");
    return this;
  }

  public CheckAssert hasId(String id) {
    assertThat(actual).isNotNull();
    assertThat(actual.getString("id")).isEqualTo(id);
    return this;
  }

  public CheckAssert hasData(String key, String value) {
    assertThat(actual).isNotNull();
    assertThat(actual.getJsonObject("data")).isNotNull().isNotEmpty();
    assertThat(actual.getJsonObject("data").getString(key)).isEqualTo(value);
    return this;
  }

  public CheckAssert hasData(String key, boolean value) {
    assertThat(actual).isNotNull();
    assertThat(actual.getJsonObject("data")).isNotNull().isNotEmpty();
    assertThat(actual.getJsonObject("data").getBoolean(key)).isEqualTo(value);
    return this;
  }

  public CheckAssert hasChildren(int size) {
    assertThat(actual).isNotNull();
    JsonArray checks = actual.getJsonArray("checks");
    assertThat(checks).hasSize(size);
    return this;
  }

  public CheckAssert hasAndGetCheck(String name) {
    assertThat(actual).isNotNull();
    JsonArray checks = actual.getJsonArray("checks");
    assertThat(checks).isNotNull().isNotEmpty();

    List<String> names = new ArrayList<>();
    for (int i = 0; i < checks.size(); i++) {
      JsonObject check = checks.getJsonObject(i);
      String id = check.getString("id");
      assertThat(id).isNotNull().isNotEmpty();
      names.add(id);
      if (name.equals(id)) {
        return new CheckAssert(check, this);
      }
    }

    fail("No check with id `" + name + "` in " + names);
    return this;
  }

  public CheckAssert done() {
    if (parent == null) {
      return this;
    }
    return parent;
  }

  public CheckAssert hasOutcomeUp() {
    assertThat(actual).isNotNull();
    String status = actual.getString("outcome");
    assertThat(status).isEqualTo("UP");
    return this;
  }

  public CheckAssert hasOutcomeDown() {
    assertThat(actual).isNotNull();
    String status = actual.getString("outcome");
    assertThat(status).isEqualTo("DOWN");
    return this;
  }
}
