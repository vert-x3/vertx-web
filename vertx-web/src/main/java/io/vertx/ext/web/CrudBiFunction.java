package io.vertx.ext.web;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.function.BiFunction;

@VertxGen
@FunctionalInterface
public interface CrudBiFunction<T,R> extends BiFunction<String, T, Future<R>> {
}
