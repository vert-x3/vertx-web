package io.vertx.ext.web;

import com.sun.istack.internal.NotNull;
import io.vertx.core.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RouteRegistration {
  @NotNull String path();
  HttpMethod[] method() default {};
}
