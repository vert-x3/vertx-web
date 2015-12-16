/**
 * This is a Proxy to emulate the SockJS client
 */

function wrapBody(body) {
  var json = JSON.stringify(body);
  return new Packages.io.vertx.core.json.JsonObject(json);
}

function unwrapMsg(msg) {
  var json = new Packages.io.vertx.core.json.JsonObject();
  var headers = new Packages.io.vertx.core.json.JsonObject();
  var msgHeaders = msg.headers();
  for (var i = msgHeaders.names().iterator();i.hasNext();) {
    var headerName = i.next();
    headers.put(headerName, msgHeaders.get(headerName));
  }
  json.put("body", msg.body());
  json.put("headers", headers);
  return JSON.parse(json.encode());
}

function wrapHeaders(headers) {
  var ret = new Packages.io.vertx.core.eventbus.DeliveryOptions();
  if (typeof headers !== 'undefined') {
    for (var name in headers) {
      if (headers.hasOwnProperty(name)) {
        ret.addHeader(name, headers[name]);
      }
    }
  }
  return ret;
}

function unwrapError(err) {
  return {
    "failureType" : err.failureType().name(),
    "failureCode" : err.failureCode(),
    "message": err.getMessage()
  };
}

function SockJS() {
  var self = this;
  setTimeout(function () {
    self.onopen && self.onopen();
  }, 1);
}

SockJS.prototype.send = function(string) {
  var json = JSON.parse(string);
  var self = this;

  if (json.type === 'ping') {
    return;
  }

  var address = json.address;
  var headers = json.headers && wrapHeaders(json.headers);
  var message = wrapBody(json.body);

  var handler = function (ar) {
    if (ar.failed()) {
      var err = unwrapError(ar.cause());
      err.type = 'err';

      if (json.replyAddress) {
        err.address = json.replyAddress;
      }
      // error messages are always delivered
      self.onmessage && self.onmessage({data: JSON.stringify(err)});
    } else {
      var result = unwrapMsg(ar.result());

      if (json.replyAddress) {
        result.address = json.replyAddress;
        self.onmessage && self.onmessage({data: JSON.stringify(result)});
      }
    }
  };

  if (address) {
    var eb = vertx._jdel.eventBus();

    switch (json.type) {
      case 'send':
        if (headers) {
          eb.send(address, message, headers, handler)
        } else {
          eb.send(address, message, handler)
        }
        break;
      case 'publish':
        if (headers) {
          eb.publish(address, message, headers)
        } else {
          eb.publish(address, message)
        }
        break;
      default:
        throw new Error('Can not understand ' + json.type);
    }
  }
};

SockJS.prototype.close = function() {
  this.onclose && this.onclose();
};

module.exports = SockJS;