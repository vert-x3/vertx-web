package io.vertx.ext.web.handler.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.*;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.FileUpload;

import java.util.Locale;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

@VertxGen
public interface UploadScalar {

  @GenIgnore(PERMITTED_TYPE)
  static GraphQLScalarType build() {
    return new GraphQLScalarType.Builder()
      .name("Upload").description("A file part in a multipart request").coercing(new Coercing<Object, Object>() {

        @Override
        public Object serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale) throws CoercingSerializeException {
          throw new CoercingSerializeException("Upload is an input-only type");
        }

        @Override
        public Object parseValue(Object o, GraphQLContext graphQLContext, Locale locale) throws CoercingParseValueException {
          if (o == null || o instanceof FileUpload) {
            return o;
          }
          throw new CoercingParseValueException("Expected type FileUpload");
        }

        @Override
        public Object parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) throws CoercingParseLiteralException {
          throw new CoercingParseLiteralException("Must use variables to specify Upload values");
        }
      }).build();
  }
}
