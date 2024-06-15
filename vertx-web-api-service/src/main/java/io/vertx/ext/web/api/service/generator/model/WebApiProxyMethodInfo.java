package io.vertx.ext.web.api.service.generator.model;

import io.vertx.codegen.processor.ParamInfo;
import io.vertx.codegen.processor.TypeParamInfo;
import io.vertx.codegen.processor.doc.Doc;
import io.vertx.codegen.processor.doc.Text;
import io.vertx.codegen.processor.type.ClassTypeInfo;
import io.vertx.codegen.processor.type.TypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class WebApiProxyMethodInfo extends ProxyMethodInfo {

  final List<ParamInfo> paramsToExtract;
  final String requestContextName;

  public WebApiProxyMethodInfo(Set<ClassTypeInfo> ownerTypes, String name, TypeInfo returnType, Text returnDescription, boolean fluent, boolean cacheReturn, List<ParamInfo> params, String comment, Doc doc, boolean staticMethod, boolean defaultMethod, List<TypeParamInfo.Method> typeParams, boolean proxyIgnore, boolean proxyClose, boolean deprecated, Text deprecatedDesc, boolean methodOverride) {
    super(ownerTypes, name, returnType, returnDescription, fluent, cacheReturn, params, comment, doc, staticMethod, defaultMethod, typeParams, proxyIgnore, proxyClose, deprecated, deprecatedDesc, methodOverride);
    final int truncateParamsToIndex = 1;
    paramsToExtract = params.subList(0, params.size() - truncateParamsToIndex);
    requestContextName = params.get(params.size() - truncateParamsToIndex).getName();
  }

  public WebApiProxyMethodInfo(ProxyMethodInfo info) {
    this(
      info.getOwnerTypes(),
      info.getName(),
      info.getReturnType(),
      info.getReturnDescription(),
      info.isFluent(),
      info.isCacheReturn(),
      info.getParams(),
      info.getComment(),
      info.getDoc(),
      info.isStaticMethod(),
      info.isDefaultMethod(),
      info.getTypeParams(),
      info.isProxyIgnore(),
      info.isProxyClose(),
      info.isDeprecated(),
      info.getDeprecatedDesc(),
      info.isMethodOverride()
    );
  }

  public List<ParamInfo> getParamsToExtract() {
    return paramsToExtract;
  }

  public String getRequestContextName() {
    return requestContextName;
  }
}
