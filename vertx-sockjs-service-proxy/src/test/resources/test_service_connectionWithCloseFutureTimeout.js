var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.createConnectionWithCloseFuture(function (err, conn) {
    if (conn === null || err) {
      vertx.eventBus().send("testaddress", "unexpected create connection error");
    } else {
      vertx.eventBus().consumer("closeCalled", function (msg) {
        if (msg.body() != "blah") {
          vertx.eventBus().send("testaddress", "unexpected close called message");
        } else {
          conn.someMethod(function (someMethodErr, someMethodRes) {
            if (!someMethodErr || someMethodErr.failureType != "NO_HANDLERS") {
              vertx.eventBus().send("testaddress", "was expecting NO_HANDLERS failure instead of " + someMethodErr.failureType);
            } else {
              vertx.eventBus().send("testaddress", "ok");
            }
          });
        }
      });
    }
  });
};