var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.setDataObjectHandler(function (err, res) {
    if (err) {
      vertx.eventBus().send("testaddress", "unexpected failure " + err);
    } else if (res[0].number != 1 && res[0].string != 'String 1' && res[0].bool != false &&
      res[1].number != 2 && res[1].string != 'String 2' && res[1].bool != true) {
      vertx.eventBus().send("testaddress", "unexpected result " + res);
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};