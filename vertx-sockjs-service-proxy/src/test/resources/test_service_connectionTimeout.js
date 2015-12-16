var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.createConnection("foo", function (err, conn) {
    if (conn == null || err) {
      vertx.eventBus().send("testaddress", "unexpected create connection error");
    } else {
      conn.startTransaction(function (startErr, startRes) {
        if (startErr) {
          vertx.eventBus().send("testaddress", "unexpected start transaction error: " + startErr);
        } else if (startRes != "foo") {
          vertx.eventBus().send("testaddress", "unexpected start transaction result: " + startRes);
        } else {
          vertx.eventBus().consumer("closeCalled", function (msg) {
            if (msg.body() != "blah") {
              vertx.eventBus().send("testaddress", "fail");
            } else {
              conn.startTransaction(function (startErr2, startRes2) {
                if (!startErr2 || startErr2.failureType != "NO_HANDLERS") {
                  vertx.eventBus().send("testaddress", "was expecting NO_HANDLERS failure instead of " + startErr2.failureType);
                } else {
                  vertx.eventBus().send("testaddress", "ok");
                }
              });
            }
          });
        }
      });
    }
  });
};