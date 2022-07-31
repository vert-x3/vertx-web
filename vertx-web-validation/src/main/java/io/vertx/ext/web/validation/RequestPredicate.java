package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.vertx.ext.web.validation.RequestPredicateResult.failed;
import static io.vertx.ext.web.validation.RequestPredicateResult.success;

/**
 * Request predicate
 */
@VertxGen
public interface RequestPredicate extends Function<RoutingContext, RequestPredicateResult> {

  /**
   * Request predicate that checks if body exists or not
   */
  RequestPredicate BODY_REQUIRED = rc -> {
    if (!rc.request().headers().contains(HttpHeaders.CONTENT_TYPE)) return failed("Body required");
    return success();
  };

  /**
   * Generate request predicate that checks if a particular multipart file upload with {@code propertyName} and matching {@code contentTypePattern} exists
   *
   * @param propertyName
   * @param contentTypePattern
   * @return
   */
  static RequestPredicate multipartFileUploadExists(String propertyName, String contentTypePattern, boolean required) {
    Pattern contentType = Pattern.compile(contentTypePattern);
    return rc -> {
      if (
        rc.request().headers().contains(HttpHeaders.CONTENT_TYPE) &&
        rc.request().getHeader(HttpHeaders.CONTENT_TYPE).contains("multipart/form-data")
      ) {
        Set<FileUpload> files = rc.fileUploads();
        FileUpload thisFile = files.stream().filter(f -> propertyName.equals(f.name()) && f.size() > 0).findFirst().orElse(null);
        if(required && (thisFile == null || thisFile.size() == 0))
          return failed(String.format("File name %s with content type %s is missing.", propertyName, contentType));
        if(thisFile != null && thisFile.size() > 0 && !contentType.matcher(thisFile.contentType()).matches())
          return failed(String.format("File name %s with content type %s is mismatch.", propertyName, contentType));
        return success();
      } else return success();
    };
  }

}
