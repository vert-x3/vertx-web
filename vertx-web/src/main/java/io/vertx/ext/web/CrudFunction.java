package io.vertx.ext.web;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.function.Function;

@VertxGen
@FunctionalInterface
public interface CrudFunction<T,R> extends Function<T, Future<R>> {
}
