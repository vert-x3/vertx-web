var EventBus = require('vertx-js/vertx-eventbus');
var bus = new EventBus();
bus.enableReconnect(true);

bus.onopen = function () {
  setTimeout(function () {
  	bus.sockJSConn.close();
  }, 250);
};

bus.onreconnect = function () {
  bus.send("the_address", {"body":"the_message"});
};
