package io.vertx.ext.web.handler.graphql;

import graphql.schema.*;
import io.vertx.ext.web.FileUpload;

public final class UploadScalar implements Coercing {
  private UploadScalar() {
  }

  public static GraphQLScalarType build() {
    return new GraphQLScalarType.Builder()
      .name("Upload").description("A file part in a multipart request").coercing(new Coercing() {

        @Override
        public Object serialize(Object o) throws CoercingSerializeException {
          throw new CoercingSerializeException("Upload is an input-only type");
        }

        @Override
        public Object parseValue(Object o) throws CoercingParseValueException {
          if (o == null || o instanceof FileUpload) {
            return o;
          }

          throw new CoercingParseValueException("Expected type FileUpload");
        }

        @Override
        public Object parseLiteral(Object o) throws CoercingParseLiteralException {
          throw new CoercingParseLiteralException("Must use variables to specify Upload values");
        }
      }).build();
  }

  @Override
  public Object serialize(Object o) throws CoercingSerializeException {
    throw new CoercingSerializeException("Upload is an input-only type");
  }

  @Override
  public Object parseValue(Object o) throws CoercingParseValueException {
    if (o == null || o instanceof FileUpload) {
      return o;
    }

    throw new CoercingParseValueException("Expected type FileUpload");
  }

  @Override
  public Object parseLiteral(Object o) throws CoercingParseLiteralException {
    throw new CoercingParseLiteralException("Must use variables to specify Upload values");
  }
}
