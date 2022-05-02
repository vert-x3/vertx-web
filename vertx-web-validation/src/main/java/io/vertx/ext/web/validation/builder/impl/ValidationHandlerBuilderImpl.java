package io.vertx.ext.web.validation.builder.impl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.RequestPredicateResult;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.BodyProcessorFactory;
import io.vertx.ext.web.validation.builder.ParameterProcessorFactory;
import io.vertx.ext.web.validation.builder.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValidationHandlerImpl;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaRepository;

import java.util.*;
import java.util.function.Function;

public class ValidationHandlerBuilderImpl implements ValidationHandlerBuilder {

  private final SchemaRepository repository;

  final Map<ParameterLocation, List<ParameterProcessor>> parameterProcessors = new HashMap<>();
  final List<BodyProcessor> bodyProcessors = new ArrayList<>();
  final List<Function<RoutingContext, RequestPredicateResult>> predicates = new ArrayList<>();

  public ValidationHandlerBuilderImpl(SchemaRepository repository) {
    Objects.requireNonNull(repository, "'repository' cannot be null");
    this.repository = repository;
  }

  @Override
  public ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor) {
    parameterProcessors.computeIfAbsent(location, k -> new ArrayList<>()).add(processor);
    return this;
  }

  @Override
  public ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor.create(ParameterLocation.QUERY, repository));
  }

  @Override
  public ValidationHandlerBuilder queryParameter(ParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor.create(ParameterLocation.QUERY, repository));
  }

  @Override
  public ValidationHandlerBuilder pathParameter(ParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.PATH, parameterProcessor.create(ParameterLocation.PATH, repository));
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor.create(ParameterLocation.COOKIE, repository));
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(ParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor.create(ParameterLocation.COOKIE, repository));
  }

  @Override
  public ValidationHandlerBuilder headerParameter(ParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.HEADER, parameterProcessor.create(ParameterLocation.HEADER, repository));
  }

  @Override
  public ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor) {
    bodyProcessors.add(bodyProcessor.create(repository));
    return this;
  }

  @Override
  public ValidationHandlerBuilder body(BodyProcessor bodyProcessor) {
    bodyProcessors.add(bodyProcessor);
    return this;
  }

  @Override
  public ValidationHandlerBuilder predicate(RequestPredicate predicate) {
    predicates.add(predicate);
    return this;
  }

  @Override
  public ValidationHandler build() {
    return new ValidationHandlerImpl(
      parameterProcessors,
      bodyProcessors,
      predicates
    );
  }
}
