@import io.vertx.core.json.JsonObject
@param JsonObject context
@param String foo
@param String bar

Hello compiled ${foo} and ${bar}
Request path is ${context.getString("path")}
