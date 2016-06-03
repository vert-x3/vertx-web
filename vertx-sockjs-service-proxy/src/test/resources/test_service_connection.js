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
        if (startRes != "foo" || startErr) {
          vertx.eventBus().send("testaddress", "unexpected start transaction error: " + startRes);
        } else {
          conn.insert("blah", {}, function (insertErr, insertRes) {
            if (insertRes != "foo" || insertErr) {
              vertx.eventBus().send("testaddress", "unexpected insert error: " + insertRes);
            } else {
              conn.commit(function (commitErr, commitRes) {
                if (commitRes != "foo" || commitErr) {
                  vertx.eventBus().send("testaddress", "unexpected commit error: " + commitRes);
                } else {
                  conn.close();
                  try {
                    conn.startTransaction(function () {
                    });
                  } catch (err) {
                    // Expected
                    vertx.eventBus().send("testaddress", "ok");
                  }
                }
              });
            }
          });
        }
      });
    }
  });
};