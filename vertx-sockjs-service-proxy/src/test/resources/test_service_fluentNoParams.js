var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  var s = null;
  vertx.eventBus().consumer("fluentReceived").handler(function (err, res) {
    if (s == testService) {
      vertx.eventBus().send("testaddress", "ok");
    } else {
      vertx.eventBus().send("testaddress", "not fluent");
    }
  });
  s = testService.fluentNoParams();
};