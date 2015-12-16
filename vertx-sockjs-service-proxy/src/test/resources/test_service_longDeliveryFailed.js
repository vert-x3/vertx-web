var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.longDeliveryFailed(function (err, res) {
    if (res != null) {
      vertx.eventBus().send("testaddress", "was expecting null result");
    } else if (err.message != 'Timed out waiting for a reply') {
      vertx.eventBus().send("testaddress", "unexpected error message <" + err.message + ">");
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};