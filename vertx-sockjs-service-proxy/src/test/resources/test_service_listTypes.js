var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.listParams(
    ["foo", "bar"],
    [12, 13],
    [123, 134],
    [1234, 1235],
    [12345, 12346],
    [{"foo": "bar"}, {"blah": "eek"}],
    [["foo"], ["blah"]],
    [{"number": 1, "string": "String 1", "bool": false}, {"number": 2, "string": "String 2", "bool": true}]
  );
};