var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.failingMethod(function (err, res) {
    if (!err) {
      vertx.eventBus().send("testaddress", "unexpected failure " + err);
    } else if (res != null) {
      vertx.eventBus().send("testaddress", "unexpected result " + res);
    } else if (err.failureType != 'RECIPIENT_FAILURE') {
      vertx.eventBus().send("testaddress", "unexpected err type  " + err.failureType);
    } else if (err.message != 'wibble') {
      vertx.eventBus().send("testaddress", "unexpected err type  " + err.message);
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};