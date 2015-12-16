var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.listCharHandler(function (err, res) {
    if (err) {
      vertx.eventBus().send("testaddress", "unexpected failure " + err);
    } else if (res[0] != 'X' && res[1] != 'Y' && res[2] != 'Z') {
      vertx.eventBus().send("testaddress", "unexpected result " + res);
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};