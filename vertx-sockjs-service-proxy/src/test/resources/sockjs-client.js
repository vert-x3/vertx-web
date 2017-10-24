/**
 * This is a Proxy to emulate the SockJS client
 */

function SockJS() {
  var self = this;
  self.client = vertx.createHttpClient({ defaultPort: 8080 });
  self.client.websocket("/eventbus/websocket", function (ws) {
    self.ws = ws;
    self.ws.handler(function (buffer) {
      self.onmessage && self.onmessage({ data: buffer.toString() });
    });
    self.ws.closeHandler(function (e) {
      self.onclose && self.onclose(e);
    });
    self.onopen && self.onopen();
  }, function (e) {
    self.onclose && self.onclose(e)
  });
}

SockJS.prototype.send = function (str) {
  if (this.ws === undefined) {
    throw "INVALID_STATE_ERR";
  }
  this.ws.writeTextMessage(str);
};

SockJS.prototype.close = function () {
  if (this.ws) {
    this.ws.close();
  }
  if (this.client) {
    this.client.close();
  }
};

module.exports = SockJS;
