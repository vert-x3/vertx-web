package io.vertx.ext.web;

import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public interface RequestParameters {

  Map<String, String> getPathParameters();

  Map<String, String> getUndecodedPathParameters();

  Map<String, String> getQueryParameters();

  Map<String, String> getUndecodedQueryParameters();

}
