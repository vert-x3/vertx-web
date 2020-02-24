package io.vertx.ext.web.api.service.generator;

import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.api.service.generator.model.WebApiProxyMethodInfo;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.serviceproxy.generator.GeneratorUtils;
import io.vertx.serviceproxy.generator.ServiceProxyHandlerGen;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class WebApiProxyHandlerGen extends ServiceProxyHandlerGen {

  private final String utilsFunctions;
  private final String serviceCallHandler;

  public WebApiProxyHandlerGen(GeneratorUtils utils) {
    super(utils);
    kinds = Collections.singleton("webapi_proxy");
    name = "web_api_service_proxy_handler";
    utilsFunctions = utils.loadResource("utils", "vertx-web-api-service-codegen");
    serviceCallHandler = utils.loadResource("service_call_handler", "vertx-web-api-service-codegen");
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Arrays.asList(WebApiServiceGen.class, ModuleGen.class);
  }

  public String className(ProxyModel model) {
    return model.getIfaceSimpleName() + "VertxProxyHandler";
  }

  @Override
  public Stream<String> additionalImports() {
    return Stream.of(ServiceRequest.class.getCanonicalName(), Optional.class.getCanonicalName());
  }

  @Override
  public void generateAdditionalMethods(ProxyModel model, CodeWriter writer) {
    super.generateAdditionalMethods(model, writer);
    writer.write(utilsFunctions);
  }

  public void generateActionSwitchEntry(ProxyMethodInfo m, CodeWriter writer) {
    if (m.isProxyClose()) {
      super.generateActionSwitchEntry(m, writer);
      return;
    }
    TypeInfo returnType = m.getReturnType();
    boolean returnFuture = ProxyModel.isFuture(returnType);
    writer
      .codeln(String.format("case \"%s\": {", m.getName()))
      .indent()
      .stmt("JsonObject contextSerialized = json.getJsonObject(\"context\")")
      .codeln("if (contextSerialized == null)")
      .indent()
      .stmt("throw new IllegalStateException(\"Received action \" + action + \" without ServiceRequest \\\"context\\\"\")")
      .unindent()
      .stmt("ServiceRequest context = new ServiceRequest(contextSerialized)")
      .stmt("JsonObject params = context.getParams()")
      .codeln("try {")
      .indent()
      .code(String.format("service.%s(", m.getName()))
      .indent();

    Stream<String> methodParamsTail;
    if (returnFuture) {
      methodParamsTail = Stream.of("context");
    } else {
      methodParamsTail = Stream.of("context", serviceCallHandler);
    }

    writer.writeSeq(
      Stream.concat(
        ((WebApiProxyMethodInfo) m).getParamsToExtract().stream().map(this::generateJsonParamExtractFromContext),
        methodParamsTail
      ),
      ",\n" + writer.indentation()
    );
    writer.unindent();
    if (returnFuture) {
      writer.print(")");
      writer.println(".setHandler(" + generateFutureHandler((ParameterizedTypeInfo) returnType) + ");");
    } else {
      writer.write(");\n");
    }
    writer.unindent()
      .codeln("} catch (Exception e) {")
      .indent()
      .stmt("HelperUtils.manageFailure(msg, e, includeDebugInfo)")
      .unindent()
      .codeln("}");
    if (m.isProxyClose()) writer.stmt("close()");
    writer.stmt("break");
    writer.unindent();
    writer.codeln("}");
  }

  public String generateJsonParamExtractFromContext(ParamInfo param) {
    String name = param.getName();
    TypeInfo type = param.getType();
    String typeName = type.getName();
    if (type.isDataObjectHolder()) {
      switch (type.getDataObject().getDeserializer().getKind()) {
        case SELF:
          return "searchOptionalInJson(params, \"" + name + "\").map(j -> (" + type.getDataObject().getJsonType().getName() + ")j).map(j -> new " + type.getName() + "(j)).orElse(null)";
        case STATIC_METHOD:
          return "searchOptionalInJson(params, \"" + name + "\").map(j -> (" + type.getDataObject().getJsonType().getName() + ")j).map(j -> " + type.getDataObject().getDeserializer().getQualifiedName() + "(j)).orElse(null)";
      }
    }
    if (typeName.equals(RequestParameter.class.getName()))
      return "io.vertx.ext.web.validation.RequestParameter.create(searchInJson(params, \"" + name + "\"))";
    if (typeName.equals("char") || typeName.equals("java.lang.Character"))
      return "searchCharInJson(params, \"" + name + "\")";
    if (typeName.equals("byte") || typeName.equals("java.lang.Byte") ||
      typeName.equals("short") || typeName.equals("java.lang.Short") ||
      typeName.equals("int") || typeName.equals("java.lang.Integer") ||
      typeName.equals("long") || typeName.equals("java.lang.Long"))
      return "searchOptionalLongInJson(params, \"" + name + "\").map(Long::" + numericMapping.get(typeName) + "Value).orElse(null)";
    if (typeName.equals("float") || typeName.equals("java.lang.Float") ||
      typeName.equals("double") || typeName.equals("java.lang.Double"))
      return "searchOptionalDoubleInJson(params, \"" + name + "\").map(Double::" + numericMapping.get(typeName) + "Value).orElse(null)";
    if (type.getKind() == ClassKind.ENUM)
      return "searchOptionalStringInJson(params, \"" + name + "\").map(s -> " + param.getType().getName() + ".valueOf(s)).orElse(null)";
    if (type.getKind() == ClassKind.LIST || type.getKind() == ClassKind.SET) {
      String coll = type.getKind() == ClassKind.LIST ? "List" : "Set";
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(0);
      if (typeArg.isDataObjectHolder()) {
        switch (typeArg.getDataObject().getDeserializer().getKind()) {
          case SELF:
            return "searchOptionalJsonArrayInJson(params, \"" + name + "\").map(a -> a.stream().map(o -> new " + typeArg.getName() + "((" + typeArg.getDataObject().getJsonType().getName() + ")o)).collect(Collectors.to" + coll + "())).orElse(null)";
          case STATIC_METHOD:
            return "searchOptionalJsonArrayInJson(params, \"" + name + "\").map(a -> a.stream().map(o -> " + typeArg.getDataObject().getDeserializer().getQualifiedName() + "((" + typeArg.getDataObject().getJsonType().getName() + ")o)).collect(Collectors.to" + coll + "())).orElse(null)";
        }
      }
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long"))
        return "searchOptionalJsonArrayInJson(params, \"" + name + "\").map(a -> a.stream().map(o -> ((Number)o)." + numericMapping.get(typeArg.getName()) + "Value()).collect(Collectors.to" + coll + "())).orElse(null)";
      return "HelperUtils.convert" + coll + "(searchOptionalJsonArrayInJson(params, \"" + name + "\").map(JsonArray::getList).orElse(null))";
    }
    if (type.getKind() == ClassKind.MAP) {
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(1);
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long") ||
        typeArg.getName().equals("java.lang.Float") || typeArg.getName().equals("java.lang.Double"))
        return "searchOptionalJsonObjectInJson(params, \"" + name + "\").map(m -> m.getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue())." + numericMapping.get(typeArg.getName()) + "Value()))).orElse(null)";
      return "HelperUtils.convertMap(searchOptionalJsonObjectInJson(params, \"" + name + "\").map(JsonObject::getMap).orElse(null))";
    }
    return "(" + type.getName() + ")searchInJson(params, \"" + name + "\")";
  }

}
