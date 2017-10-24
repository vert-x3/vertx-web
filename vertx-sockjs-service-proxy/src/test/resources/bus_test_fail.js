var EventBus = require('vertx-js/vertx-eventbus');
var bus = new EventBus();

bus.onopen = function () {
  bus.send("client_registered", {}, function (err, res) {
    res.fail(520, "client_failure_message");
  });
};
