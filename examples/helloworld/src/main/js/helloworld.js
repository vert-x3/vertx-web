
var vertx = require("vertx-js/vertx");
var Router = require("vertx-apex-js/router");

var router = Router.router(vertx);
router.route().handler(function(routingContext) {
  routingContext.response().putHeader("content-type", "text/plain").end("Hello World!");
});

server.requestHandler(router.accept).listen(8080);
