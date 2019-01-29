package io.vertx.ext.web.api.generator.impl.model;

import io.vertx.codegen.*;
import io.vertx.codegen.doc.Doc;
import io.vertx.codegen.doc.Text;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class WebApiProxyModel extends ProxyModel {

  public WebApiProxyModel(ProcessingEnvironment env, TypeElement modelElt) {
    super(env, modelElt);
  }

  @Override
  public String getKind() {
    return "webapi_proxy";
  }

  @Override
  protected void checkParamType(ExecutableElement elem, TypeMirror type, TypeInfo typeInfo, int pos, int numParams, boolean allowAnyJavaType) {
    // We allow RequestParameter but not as last or before last parameter
    if (RequestParameter.class.getName().equals(typeInfo.getName()) && (numParams - pos) > 2)
      return;
    super.checkParamType(elem, type, typeInfo, pos, numParams, allowAnyJavaType);
  }

  @Override
  protected MethodInfo createMethodInfo(Set<ClassTypeInfo> ownerTypes, String methodName, String comment, Doc doc, TypeInfo returnType, Text returnDescription, boolean isFluent, boolean isCacheReturn, List<ParamInfo> mParams, ExecutableElement methodElt, boolean isStatic, boolean isDefault, ArrayList<TypeParamInfo.Method> typeParams, TypeElement declaringElt, boolean methodDeprecated, Text deprecatedDesc) {
    ProxyMethodInfo baseInfo = (ProxyMethodInfo) super.createMethodInfo(ownerTypes, methodName, comment, doc, returnType, returnDescription, isFluent, isCacheReturn, mParams, methodElt, isStatic, isDefault, typeParams, declaringElt, methodDeprecated, deprecatedDesc);
    if (!isStatic && !baseInfo.isProxyClose()) {
      if (mParams.size() < 2) {
        throw new GenException(this.modelElt, "Method should have second to last parameter of type io.vertx.ext.web.api.OperationRequest and last parameter of type Handler<AsyncResult<io.vertx.ext.web.api.OperationResponse>>");
      }
      ParamInfo shouldBeRequestContextParam = mParams.get(mParams.size() - 2);
      if (shouldBeRequestContextParam == null || !shouldBeRequestContextParam.getType().getName().equals(OperationRequest.class.getName())) {
        throw new GenException(this.modelElt, "Method " + methodName + "should have the second to last parameter with type io.vertx.ext.web.api.RequestContext");
      }
      ParamInfo shouldBeHandler = mParams.get(mParams.size() - 1);
      if (baseInfo.getKind() != MethodKind.HANDLER || shouldBeHandler == null) {
        TypeInfo shouldBeOperationResult = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) shouldBeHandler.getType()).getArg(0)).getArg(0);
        if (!OperationResponse.class.getName().equals(shouldBeOperationResult.getName()))
          throw new GenException(this.modelElt, "Method " + methodName + "should last parameter should be an handler of type Handler<AsyncResult<io.vertx.ext.web.api.OperationResponse>>");
      }
      return new WebApiProxyMethodInfo(baseInfo);
    } else {
      return baseInfo;
    }
  }
}
