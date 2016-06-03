var EventBus = require('vertx-js/vertx-eventbus');
var TestService = require('test-js/test_service-proxy');

var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.basicBoxedTypes("foo", 123, 1234, 12345, 123456, 12.34, 12.3456, 'X', true);
};

