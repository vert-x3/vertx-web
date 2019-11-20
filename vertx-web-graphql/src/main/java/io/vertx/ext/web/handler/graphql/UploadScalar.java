package io.vertx.ext.web.handler.graphql;

import graphql.schema.*;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.FileUpload;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

@VertxGen
public interface UploadScalar extends Coercing {

  @GenIgnore(PERMITTED_TYPE)
  static GraphQLScalarType build() {
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

  /**
   * @deprecated Use the factory method to create a coercing scalar object.
   */
  @Override
  @Deprecated
  default Object serialize(Object o) throws CoercingSerializeException {
    throw new CoercingSerializeException("Upload is an input-only type");
  }

  /**
   * @deprecated Use the factory method to create a coercing scalar object.
   */
  @Override
  @Deprecated
  default Object parseValue(Object o) throws CoercingParseValueException {
    if (o == null || o instanceof FileUpload) {
      return o;
    }

    throw new CoercingParseValueException("Expected type FileUpload");
  }

  /**
   * @deprecated Use the factory method to create a coercing scalar object.
   */
  @Override
  @Deprecated
  default Object parseLiteral(Object o) throws CoercingParseLiteralException {
    throw new CoercingParseLiteralException("Must use variables to specify Upload values");
  }
}
