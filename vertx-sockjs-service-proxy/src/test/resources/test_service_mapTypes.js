var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.mapParams(
    {"eek": "foo", "wob": "bar"},
    {"eek": 12, "wob": 13},
    {"eek": 123, "wob": 134},
    {"eek": 1234, "wob": 1235},
    {"eek": 12345, "wob": 12356},
    {"eek": {"foo": "bar"}, "wob": {"blah": "eek"}},
    {"eek": ["foo"], "wob": ["blah"]}
  );
};