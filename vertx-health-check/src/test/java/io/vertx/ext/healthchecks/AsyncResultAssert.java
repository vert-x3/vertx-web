package io.vertx.ext.healthchecks;

import io.vertx.core.AsyncResult;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AsyncResultAssert<T> extends AbstractAssert<AsyncResultAssert<T>, AsyncResult<T>> {

  public AsyncResultAssert(AsyncResult<T> actual) {
    super(actual, AsyncResultAssert.class);
  }

  public AsyncResultAssert<T> succeeded() {
    assertThat(actual.succeeded()).isTrue();
    return this;
  }

  public AsyncResultAssert<T> failed() {
    assertThat(actual.failed()).isTrue();
    return this;
  }

  public AsyncResultAssert<T> hasNoContent(T t) {
    assertThat(actual.result()).isNull();
    return this;
  }

  public AsyncResultAssert<T> hasContent(T t) {
    assertThat(actual.result()).isEqualTo(t);
    return this;
  }

  public AsyncResultAssert<T> hasContent() {
    assertThat(actual.result()).isNotNull();
    return this;
  }


}
