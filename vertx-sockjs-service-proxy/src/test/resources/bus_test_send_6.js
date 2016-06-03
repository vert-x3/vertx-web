var EventBus = require('vertx-js/vertx-eventbus');
var bus = new EventBus();

bus.onopen = function () {
  bus.send("the_address", {"body": "the_message"}, {"the_header_name": "the_header_value"}, function (err, res) {
    bus.send("the_address_fail", {"body": "the_message"}, {"the_header_name": "the_header_value_fail"}, function (err, res) {
      if (err) {
        bus.send("done", {"body": "ok"});
      }
    });
  });
};