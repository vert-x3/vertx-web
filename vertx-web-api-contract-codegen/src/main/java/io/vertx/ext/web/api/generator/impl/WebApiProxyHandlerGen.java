package io.vertx.ext.web.api.generator.impl;

import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.*;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.impl.model.WebApiProxyMethodInfo;
import io.vertx.serviceproxy.generator.CodeWriter;
import io.vertx.serviceproxy.generator.GeneratorUtils;
import io.vertx.serviceproxy.generator.ServiceProxyHandlerGen;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://slinkydeveloper.github.io">Francesco Guardiani @slinkydeveloper</a>
 */
public class WebApiProxyHandlerGen extends ServiceProxyHandlerGen {

  private final String serviceCallHandler;

  public WebApiProxyHandlerGen(GeneratorUtils utils) {
    super(utils);
    kinds = Collections.singleton("webapi_proxy");
    name = "web_api_service_proxy_handler";
    serviceCallHandler = utils.loadResource("service_call_handler");
  }

  public String className(ProxyModel model) {
    return model.getIfaceSimpleName() + "VertxProxyHandler";
  }

  public Stream<String> additionalImports(ProxyModel model) {
    return Stream.concat(
      model.getImportedTypes().stream().filter(c -> !c.getPackageName().equals("java.lang")).map(ClassTypeInfo::toString),
      Stream.of("io.vertx.ext.web.api.RequestContext", "io.vertx.ext.web.api.generator.ApiHandlerUtils")
    );
  }

  public void generateActionSwitchEntry(ProxyMethodInfo m, CodeWriter writer) {
    if (m.isProxyClose()) {
      super.generateActionSwitchEntry(m, writer);
      return;
    }
    writer
      .code("case \"" + m.getName() + "\": {\n")
      .indent()
      .stmt("JsonObject contextSerialized = json.getJsonObject(\"context\")")
      .code("if (contextSerialized == null)\n")
      .indent()
      .stmt("throw new IllegalStateException(\"Received action \" + action + \" without RequestContext \\\"context\\\"\")")
      .unindent()
      .stmt("RequestContext context = new RequestContext(contextSerialized)")
      .stmt("JsonObject params = context.getParams()")
      .code("try {\n")
      .indent()
      .code("service." + m.getName() + "(")
      .indent();
    writer.writeArray(
      ",\n" + writer.indentation(),
      Stream.concat(
        ((WebApiProxyMethodInfo)m).getParamsToExtract().stream().map(this::generateJsonParamExtractFromContext),
        Stream.of("context", serviceCallHandler)
      ).collect(Collectors.toList()),
      String::toString
    );
    writer.unindent();
    writer.write(");\n");
    writer.unindent()
      .code("} catch (Exception e) {\n")
      .indent()
      .stmt("msg.reply(new ServiceException(-1, e.getMessage()))")
      .unindent()
      .code("}\n");
    if (m.isProxyClose()) writer.stmt("close()");
    writer.stmt("break");
    writer.unindent();
    writer.code("}\n");
  }

  public String generateJsonParamExtractFromContext(ParamInfo param) {
    String name = param.getName();
    TypeInfo type = param.getType();
    String typeName = type.getName();
    if (typeName.equals(RequestParameter.class.getName()))
      return "io.vertx.ext.web.api.RequestParameter.create(\"" + name + "\", ApiHandlerUtils.searchInJson(params, \"" + name + "\"))";
    if (typeName.equals("char") || typeName.equals("java.lang.Character"))
      return "ApiHandlerUtils.searchOptionalIntegerInJson(params, \"" + name + "\").map(i -> (char)(int)i).get()";
    if (typeName.equals("byte") || typeName.equals("java.lang.Byte") ||
      typeName.equals("short") || typeName.equals("java.lang.Short") ||
      typeName.equals("int") || typeName.equals("java.lang.Integer") ||
      typeName.equals("long") || typeName.equals("java.lang.Long"))
      return "ApiHandlerUtils.searchOptionalLongInJson(params, \"" + name + "\").map(Long::" + numericMapping.get(typeName) + "Value).get()";
    if (typeName.equals("float") || typeName.equals("java.lang.Float") ||
      typeName.equals("double") || typeName.equals("java.lang.Double"))
      return "ApiHandlerUtils.searchOptionalDoubleInJson(params, \"" + name + "\").map(Double::" + numericMapping.get(typeName) + "Value).get()";
    if (type.getKind() == ClassKind.ENUM)
      return "ApiHandlerUtils.searchOptionalStringInJson(params, \"" + name + "\").map(s -> " + param.getType().getName() + ".valueOf(s)).get()";
    if (type.getKind() == ClassKind.LIST || type.getKind() == ClassKind.SET) {
      String coll = type.getKind() == ClassKind.LIST ? "List" : "Set";
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(0);
      if (typeArg.getKind() == ClassKind.DATA_OBJECT)
        return "ApiHandlerUtils.searchOptionalJsonArrayInJson(params, \"" + name + "\").map(a -> a.stream().map(o -> new " + typeArg.getName() + "((JsonObject)o)).collect(Collectors.to" + coll + "())).get()";
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long"))
        return "ApiHandlerUtils.searchOptionalJsonArrayInJson(params, \"" + name + "\").map(a -> a.stream().map(o -> ((Number)o)." + numericMapping.get(typeArg.getName()) + "Value()).collect(Collectors.to" + coll + "())).get()";
      return "HelperUtils.convert" + coll + "(ApiHandlerUtils.searchOptionalJsonArrayInJson(params, \"" + name + "\").map(JsonArray::getList).get())";
    }
    if (type.getKind() == ClassKind.MAP) {
      TypeInfo typeArg = ((ParameterizedTypeInfo)type).getArg(1);
      if (typeArg.getName().equals("java.lang.Byte") || typeArg.getName().equals("java.lang.Short") ||
        typeArg.getName().equals("java.lang.Integer") || typeArg.getName().equals("java.lang.Long") ||
        typeArg.getName().equals("java.lang.Float") || typeArg.getName().equals("java.lang.Double"))
        return "ApiHandlerUtils.searchOptionalJsonObjectInJson(params, \"" + name + "\").map(m -> m.getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((java.lang.Number)entry.getValue())." + numericMapping.get(typeArg.getName()) + "Value()))).get()";
      return "HelperUtils.convertMap(ApiHandlerUtils.searchOptionalJsonObjectInJson(params, \"" + name + "\").map(JsonObject::getMap).get())";
    }
    if (type.getKind() == ClassKind.DATA_OBJECT)
      return "ApiHandlerUtils.searchOptionalJsonObjectInJson(params, \"" + name + "\").map(j -> new " + type.getName() + "(j)).get()";
    return "(" + type.getName() + ")ApiHandlerUtils.searchInJson(params, \"" + name + "\")";
  }

}
