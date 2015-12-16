var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.longDeliverySuccess(function (err, res) {
    if (err) {
      vertx.eventBus().send("testaddress", "no err expected");
    } else if (res != 'blah') {
      vertx.eventBus().send("testaddress", "bad result blah != " + res);
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};