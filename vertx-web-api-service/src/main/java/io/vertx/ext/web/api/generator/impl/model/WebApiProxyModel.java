package io.vertx.ext.web.api.generator.impl.model;

import io.vertx.codegen.*;
import io.vertx.codegen.doc.Doc;
import io.vertx.codegen.doc.Text;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.codegen.type.TypeMirrorFactory;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
public class WebApiProxyModel extends ProxyModel {

  private static String SIGNATURE_CONSTRAINT_ERROR = "Method must respect signature Future<io.vertx.ext.web.api.OperationResponse> foo(extractedParams..., io.vertx.ext.web.api.OperationRequest request) or foo(extractedParams..., io.vertx.ext.web.api.OperationRequest request, Handler<AsyncResult<io.vertx.ext.web.api.OperationResponse>> handler)";

  public WebApiProxyModel(ProcessingEnvironment env, TypeMirrorFactory typeFactory,TypeElement modelElt) {
    super(env, typeFactory, modelElt);
  }

  @Override
  public String getKind() {
    return "webapi_proxy";
  }

  @Override
  protected void checkParamType(ExecutableElement elem, TypeInfo typeInfo, int pos, int numParams, boolean allowAnyJavaType) {
    // We allow RequestParameter but not as last or before last parameter
    if (RequestParameter.class.getName().equals(typeInfo.getName()))
      return;
    super.checkParamType(elem, typeInfo, pos, numParams, allowAnyJavaType);
  }

  @Override
  protected MethodInfo createMethodInfo(Set<ClassTypeInfo> ownerTypes, String methodName, String comment, Doc doc, TypeInfo returnType, Text returnDescription, boolean isFluent, boolean isCacheReturn, List<ParamInfo> mParams, ExecutableElement methodElt, boolean isStatic, boolean isDefault, ArrayList<TypeParamInfo.Method> typeParams, TypeElement declaringElt, boolean methodDeprecated, Text deprecatedDesc) {
    ProxyMethodInfo baseInfo = (ProxyMethodInfo) super.createMethodInfo(ownerTypes, methodName, comment, doc, returnType, returnDescription, isFluent, isCacheReturn, mParams, methodElt, isStatic, isDefault, typeParams, declaringElt, methodDeprecated, deprecatedDesc);
    if (!isStatic && !baseInfo.isProxyClose()) {
      // Check signature constraints

      TypeInfo ret;
      if (ProxyModel.isFuture(returnType)) {
        ret = ((ParameterizedTypeInfo)returnType).getArg(0);
      } else if (baseInfo.getKind() == MethodKind.FUTURE) {
        ret = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) mParams.get(mParams.size() - 1).getType()).getArg(0)).getArg(0);
      } else {
        throw new GenException(methodElt, SIGNATURE_CONSTRAINT_ERROR);
      }
      if (!OperationResponse.class.getName().equals(ret.getName())) {
        throw new GenException(methodElt, SIGNATURE_CONSTRAINT_ERROR);
      }

      TypeInfo shouldBeOperationRequest;
      if (baseInfo.getKind() == MethodKind.FUTURE) {
        if (mParams.size() <= 1) {
          throw new GenException(methodElt, SIGNATURE_CONSTRAINT_ERROR);
        }
        shouldBeOperationRequest = mParams.get(mParams.size() - 2).getType();
      } else {
        if (mParams.size() == 0) {
          throw new GenException(methodElt, SIGNATURE_CONSTRAINT_ERROR);
        }
        shouldBeOperationRequest = mParams.get(mParams.size() - 1).getType();
      }
      if (!OperationRequest.class.getName().equals(shouldBeOperationRequest.getName())) {
        throw new GenException(methodElt, SIGNATURE_CONSTRAINT_ERROR);
      }

      return new WebApiProxyMethodInfo(baseInfo);
    } else {
      return baseInfo;
    }
  }
}
