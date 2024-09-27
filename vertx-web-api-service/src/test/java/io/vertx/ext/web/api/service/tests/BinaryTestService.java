package io.vertx.ext.web.api.service.tests;

import io.vertx.core.Future;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

@WebApiServiceGen
public interface BinaryTestService {

  @Deprecated
  Future<ServiceResponse> binaryTest(final ServiceRequest request);

}
