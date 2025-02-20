package io.vertx.ext.web;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public interface MIMEHeader extends ParsedHeaderValue {

  /**
   * Gets the parsed component part of the MIME. This is the string between the beginning and the first {@code '/'}
   * of the MIME
   * @return The component of the MIME this represents
   */
  String component();

  /**
   * Gets the parsed subcomponent part of the MIME. This is the string between the first {@code '/'} and the
   * {@code ';'} or the end of the MIME
   * @return The subcomponent of the MIME this represents
   */
  String subComponent();

  /**
   * Gets the MIME media type string.
   * This includes both the component and subcomponent parts of the MIME type.
   * @return The MIME media type string.
   */
  String mediaType();

  /**
   * Gets the MIME media type string with parameters attached.
   * This includes both the component and subcomponent parts of the MIME type, and parameters.
   * @return The MIME media type string.
   */
  String mediaTypeWithParams();

}
