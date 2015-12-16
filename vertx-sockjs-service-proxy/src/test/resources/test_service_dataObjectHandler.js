var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.dataObjectHandler(function (err, res) {
    if (err) {
      vertx.eventBus().send("testaddress", "unexpected failure " + err);
    } else if (res.string != 'foo' && res.number != 123 && res.bool != true) {
      vertx.eventBus().send("testaddress", "unexpected result " + res);
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};