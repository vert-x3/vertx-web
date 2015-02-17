/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex.handler.sockjs.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.sockjs.BridgeOptions;
import io.vertx.ext.apex.handler.sockjs.EventBusBridgeHook;
import io.vertx.ext.apex.handler.sockjs.SockJSSocket;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EventBusBridgeImpl implements Handler<SockJSSocket> {

  private static final Logger log = LoggerFactory.getLogger(EventBusBridgeImpl.class);

  private final Map<SockJSSocket, SockInfo> sockInfos = new HashMap<>();
  private final List<JsonObject> inboundPermitted;
  private final List<JsonObject> outboundPermitted;
  private final int maxAddressLength;
  private final int maxHandlersPerSocket;
  private final long pingTimeout;
  private final long replyTimeout;
  private final Vertx vertx;
  private final EventBus eb;
  private final Set<String> acceptedReplyAddresses = new HashSet<>();
  private final Map<String, Pattern> compiledREs = new HashMap<>();
  private EventBusBridgeHook hook;

  public EventBusBridgeImpl(Vertx vertx, BridgeOptions options) {
    this.vertx = vertx;
    this.eb = vertx.eventBus();
    this.inboundPermitted = options.getInboundPermitteds() == null ? new ArrayList<>() : options.getInboundPermitteds();
    this.outboundPermitted = options.getOutboundPermitteds() == null ? new ArrayList<>() : options.getOutboundPermitteds();
    this.maxAddressLength = options.getMaxAddressLength();
    this.maxHandlersPerSocket = options.getMaxHandlersPerSocket();
    this.pingTimeout = options.getPingTimeout();
    this.replyTimeout = options.getReplyTimeout();
    validatePermitted();
  }

  private void handleSocketClosed(SockJSSocket sock, Map<String, MessageConsumer> registrations) {
    // On close unregister any handlers that haven't been unregistered
    registrations.entrySet().forEach(entry -> {
      handleUnregister(sock, entry.getKey());
      entry.getValue().unregister();
    });

    SockInfo info = sockInfos.remove(sock);
    if (info != null) {
      PingInfo pingInfo = info.pingInfo;
      if (pingInfo != null) {
        vertx.cancelTimer(pingInfo.timerID);
      }
    }

    handleSocketClosed(sock);
  }

  private void handleSocketData(SockJSSocket sock, Buffer data, Map<String, MessageConsumer> registrations) {
    JsonObject msg;

    try {
      msg = new JsonObject(data.toString());
    } catch (DecodeException e) {
      replyError(sock, "invalid_json");
      return;
    }

    String type = msg.getString("type");
    if (type == null) {
      replyError(sock, "missing_type");
      return;
    }

    if (type.equals("ping")) {
      internalHandlePing(sock);
    } else {
      String address = msg.getString("address");
      if (address == null) {
        replyError(sock, "missing_address");
        return;
      }
      switch (type) {
        case "send":
          internalHandleSendOrPub(sock, true, msg, address);
          break;
        case "publish":
          internalHandleSendOrPub(sock, false, msg, address);
          break;
        case "register":
          internalHandleRegister(sock, address, registrations);
          break;
        case "unregister":
          internalHandleUnregister(sock, address, registrations);
          break;
        default:
          log.error("Invalid type in incoming message: " + type);
          replyError(sock, "invalid_type");
      }
    }

  }

  private String getAddress(JsonObject msg, SockJSSocket sock) {
    String address = msg.getString("address");
    if (address == null) {
      replyError(sock, "missing_address");
    }
    return address;
  }

  private void internalHandleSendOrPub(SockJSSocket sock, boolean send, JsonObject msg, String address) {
    if (handleSendOrPub(sock, send, msg, address)) {
      doSendOrPub(send, sock, address, msg);
    }
  }

  private boolean checkMaxHandlers(SockJSSocket sock, SockInfo info) {
    if (info.handlerCount < maxHandlersPerSocket) {
      return true;
    } else {
      log.warn("Refusing to register as max_handlers_per_socket reached already");
      replyError(sock, "max_handlers_reached");
      return false;
    }
  }

  private void internalHandleRegister(SockJSSocket sock, String address, Map<String, MessageConsumer> registrations) {
    if (address.length() > maxAddressLength) {
      log.warn("Refusing to register as address length > max_address_length");
      replyError(sock, "max_address_length_reached");
      return;
    }
    final SockInfo info = sockInfos.get(sock);
    if (!checkMaxHandlers(sock, info)) {
      return;
    }
    if (handlePreRegister(sock, address)) {
      final boolean debug = log.isDebugEnabled();
      Match match = checkMatches(false, address, null);
      if (match.doesMatch) {
        Handler<Message<Object>> handler = msg -> {
          Match curMatch = checkMatches(false, address, msg.body());
          if (curMatch.doesMatch) {
            if (curMatch.requiredPermission != null || curMatch.requiredRole != null) {
              authorise(curMatch, sock.apexSession(), res -> {
                if (res.succeeded()) {
                  if (res.result()) {
                    checkAddAccceptedReplyAddress(msg.replyAddress());
                    deliverMessage(sock, address, msg);
                  } else {
                    if (debug) {
                      log.debug("Outbound message for address " + address + " rejected because auth is required and socket is not authed");
                    }
                  }
                } else {
                  log.error(res.cause());
                }
              });

            } else {
              checkAddAccceptedReplyAddress(msg.replyAddress());
              deliverMessage(sock, address, msg);
            }
          } else {
            // outbound match failed
            if (debug) {
              log.debug("Outbound message for address " + address + " rejected because there is no inbound match");
            }
          }
        };
        MessageConsumer reg = eb.consumer(address).handler(handler);
        registrations.put(address, reg);
        handlePostRegister(sock, address);
        info.handlerCount++;
      } else {
        // inbound match failed
        if (debug) {
          log.debug("Cannot register handler for address " + address + " because there is no inbound match");
        }
        replyError(sock, "access_denied");
      }
    }
  }

  private void internalHandleUnregister(SockJSSocket sock, String address, Map<String, MessageConsumer> registrations) {
    if (handleUnregister(sock, address)) {
      Match match = checkMatches(false, address, null);
      if (match.doesMatch) {
        MessageConsumer reg = registrations.remove(address);
        if (reg != null) {
          reg.unregister();
          SockInfo info = sockInfos.get(sock);
          info.handlerCount--;
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Cannot unregister handler for address " + address + " because there is no inbound match");
        }
        replyError(sock, "access_denied");
      }
    }
  }

  private void internalHandlePing(final SockJSSocket sock) {
    SockInfo info = sockInfos.get(sock);
    if (info != null) {
      info.pingInfo.lastPing = System.currentTimeMillis();
    }
  }

  public void handle(final SockJSSocket sock) {
    if (!handleSocketCreated(sock)) {
      sock.close();
    } else {
      final Map<String, MessageConsumer> registrations = new HashMap<>();

      sock.endHandler(v ->  handleSocketClosed(sock, registrations));
      sock.handler(data ->  handleSocketData(sock, data, registrations));

      // Start a checker to check for pings
      final PingInfo pingInfo = new PingInfo();
      pingInfo.timerID = vertx.setPeriodic(pingTimeout, id -> {
        if (System.currentTimeMillis() - pingInfo.lastPing >= pingTimeout) {
          // We didn't receive a ping in time so close the socket
          sock.close();
        }
      });
      SockInfo sockInfo = new SockInfo();
      sockInfo.pingInfo = pingInfo;
      sockInfos.put(sock, sockInfo);
    }
  }

  private void checkAddAccceptedReplyAddress(final String replyAddress) {
    if (replyAddress != null) {
      // This message has a reply address
      // When the reply comes through we want to accept it irrespective of its address
      // Since all replies are implicitly accepted if the original message was accepted
      // So we cache the reply address, so we can check against it
      acceptedReplyAddresses.add(replyAddress);
      // And we remove after timeout in case the reply never comes
      vertx.setTimer(replyTimeout, id ->  acceptedReplyAddresses.remove(replyAddress));
    }
  }

  private static void deliverMessage(SockJSSocket sock, String address, Message message) {
    JsonObject envelope = new JsonObject().put("type", "rec").put("address", address).put("body", message.body());
    if (message.replyAddress() != null) {
      envelope.put("replyAddress", message.replyAddress());
    }
    sock.write(buffer(envelope.encode()));
  }

  private void doSendOrPub(boolean send, SockJSSocket sock, String address,
                           JsonObject message) {
    final Object body = message.getValue("body");
    final String replyAddress = message.getString("replyAddress");
    // Sanity check reply address is not too big, to avoid DoS
    if (replyAddress != null && replyAddress.length() > 36) {
      // vertxbus.js ids are always 36 chars
      log.error("Will not send message, reply address is > 36 chars");
      replyError(sock, "invalid_reply_address");
      return;
    }
    final boolean debug = log.isDebugEnabled();
    if (debug) {
      log.debug("Received msg from client in bridge. address:"  + address + " message:" + body);
    }
    Match curMatch = checkMatches(true, address, body);
    if (curMatch.doesMatch) {
      if (curMatch.requiredPermission != null || curMatch.requiredRole != null) {
        Session apexSession = sock.apexSession();
        if (apexSession != null) {
          if (!apexSession.isLoggedIn()) {
            replyError(sock, "not_logged_in");
          } else {
            authorise(curMatch, apexSession, res -> {
              if (res.succeeded()) {
                if (res.result()) {
                  checkAndSend(send, address, body, sock, replyAddress);
                } else {
                  replyError(sock, "access_denied");
                  if (debug) {
                    log.debug("Inbound message for address " + address + " rejected because is not authorised");
                  }
                }
              } else {
                replyError(sock, "auth_error");
                log.error("Error in performing authorisation", res.cause());
              }
            });
          }
        } else {
          // no apex session
          replyError(sock, "no_session");
          if (debug) {
            log.debug("Inbound message for address " + address +
                      " rejected because it requires auth and there is no apex session");
          }
        }
      } else {
        checkAndSend(send, address, body, sock, replyAddress);
      }
    } else {
      // inbound match failed
      replyError(sock, "access_denied");
      if (debug) {
        log.debug("Inbound message for address " + address + " rejected because there is no match");
      }
    }
  }

  private void checkAndSend(boolean send, String address, Object body,
                            SockJSSocket sock,
                            String replyAddress) {
    final SockInfo info = sockInfos.get(sock);
    if (replyAddress != null && !checkMaxHandlers(sock, info)) {
      return;
    }
    final Handler<AsyncResult<Message<Object>>> replyHandler;
    if (replyAddress != null) {
      replyHandler = result -> {
        if (result.succeeded()) {
          Message message = result.result();
          // Note we don't check outbound matches for replies
          // Replies are always let through if the original message
          // was approved
          checkAddAccceptedReplyAddress(message.replyAddress());
          deliverMessage(sock, replyAddress, message);
        } else {
          ReplyException cause = (ReplyException) result.cause();
          JsonObject envelope =
            new JsonObject().put("address", replyAddress).put("failureCode",
              cause.failureCode()).put("failureType", cause.failureType().name())
              .put("message", cause.getMessage());
          sock.write(buffer(envelope.encode()));
        }
        info.handlerCount--;
      };
    } else {
      replyHandler = null;
    }
    if (log.isDebugEnabled()) {
      log.debug("Forwarding message to address " + address + " on event bus");
    }
    if (send) {
      eb.send(address, body, new DeliveryOptions().setSendTimeout(replyTimeout), replyHandler);
      if (replyAddress != null) {
        info.handlerCount++;
      }
    } else {
      eb.publish(address, body);
    }
  }

  private void authorise(Match curMatch, Session apexSession,
                         Handler<AsyncResult<Boolean>> handler) {

    if (curMatch.requiredPermission != null) {
      apexSession.hasPermission(curMatch.requiredPermission, res -> {
        if (res.succeeded()) {
          handler.handle(Future.succeededFuture(res.result()));
        } else {
          log.error(res.cause());
        }
      });
    } else {
      apexSession.hasRole(curMatch.requiredRole, res -> {
        if (res.succeeded()) {
          handler.handle(Future.succeededFuture(res.result()));
        } else {
          log.error(res.cause());
        }
      });
    }
  }

  /*
  Empty inboundPermitted means reject everything - this is the default.
  If at least one match is supplied and all the fields of any match match then the message inboundPermitted,
  this means that specifying one match with a JSON empty object means everything is accepted
   */
  private Match checkMatches(boolean inbound, String address, Object body) {

    if (inbound && acceptedReplyAddresses.remove(address)) {
      // This is an inbound reply, so we accept it
      return new Match(true);
    }

    List<JsonObject> matches = inbound ? inboundPermitted : outboundPermitted;

    for (JsonObject matchHolder: matches) {
      String matchAddress = matchHolder.getString("address");
      String matchRegex;
      if (matchAddress == null) {
        matchRegex = matchHolder.getString("address_re");
      } else {
        matchRegex = null;
      }

      boolean addressOK;
      if (matchAddress == null) {
        if (matchRegex == null) {
          addressOK = true;
        } else {
          addressOK = regexMatches(matchRegex, address);
        }
      } else {
        addressOK = matchAddress.equals(address);
      }

      if (addressOK) {
        boolean matched = structureMatches(matchHolder.getJsonObject("match"), body);
        if (matched) {
          String requiredRole = matchHolder.getString("required_role");
          String requiredPermission = matchHolder.getString("required_permission");
          return new Match(true, requiredRole, requiredPermission);
        }
      }
    }
    return new Match(false);
  }

  private boolean regexMatches(String matchRegex, String address) {
    Pattern pattern = compiledREs.get(matchRegex);
    if (pattern == null) {
      pattern = Pattern.compile(matchRegex);
      compiledREs.put(matchRegex, pattern);
    }
    Matcher m = pattern.matcher(address);
    return m.matches();
  }

  private static void replyError(SockJSSocket sock, String err) {
    JsonObject envelope = new JsonObject().put("type", "err").put("body", err);
    sock.write(buffer(envelope.encode()));
  }

  private static boolean structureMatches(JsonObject match, Object bodyObject) {
    if (match == null || bodyObject == null) return true;

    // Can send message other than JSON too - in which case we can't do deep matching on structure of message
    if (bodyObject instanceof JsonObject) {
      JsonObject body = (JsonObject) bodyObject;
      for (String fieldName : match.fieldNames()) {
        Object mv = match.getValue(fieldName);
        Object bv = body.getValue(fieldName);
        // Support deep matching
        if (mv instanceof JsonObject) {
          if (!structureMatches((JsonObject) mv, bv)) {
            return false;
          }
        } else if (!match.getValue(fieldName).equals(body.getValue(fieldName))) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  private static class Match {
    public final boolean doesMatch;
    public final String requiredRole;
    public final String requiredPermission;

    Match(boolean doesMatch, String requiredRole, String requiredPermission) {
      this.doesMatch = doesMatch;
      this.requiredRole = requiredRole;
      this.requiredPermission = requiredPermission;
    }

    Match(boolean doesMatch) {
      this.doesMatch = doesMatch;
      this.requiredRole = null;
      this.requiredPermission = null;
    }

  }

  // Hook
  // ==============================

  public void setHook(EventBusBridgeHook hook) {
    this.hook = hook;
  }
  
  public EventBusBridgeHook getHook() {
    return hook;
  }
  
  // Override these to get hooks into the bridge events
  // ==================================================

  /**
   * The socket has been created
   * @param sock The socket
   */
  protected boolean handleSocketCreated(SockJSSocket sock) {
    if (hook != null) {
      return hook.handleSocketCreated(sock);
    } else {
      return true;
    }
  }

  /**
   * The socket has been closed
   * @param sock The socket
   */
  protected void handleSocketClosed(SockJSSocket sock) {
    if (hook != null) {
      hook.handleSocketClosed(sock);
    }
  }

  /**
   * Client is sending or publishing on the socket
   * @param sock The sock
   * @param send if true it's a send else it's a publish
   * @param msg The message
   * @param address The address the message is being sent/published to
   * @return true To allow the send/publish to occur, false otherwise
   */
  protected boolean handleSendOrPub(SockJSSocket sock, boolean send, JsonObject msg, String address) {
    if (hook != null) {
      return hook.handleSendOrPub(sock, send, msg, address);
    }
    return true;
  }

  /**
   * Client is about to register a handler
   * @param sock The socket
   * @param address The address
   * @return true to let the registration occur, false otherwise
   */
  protected boolean handlePreRegister(SockJSSocket sock, String address) {
    if (hook != null) {
      return hook.handlePreRegister(sock, address);
	  }
    return true;
  }

  /**
   * Called after client has registered
   * @param sock The socket
   * @param address The address
   */
  protected void handlePostRegister(SockJSSocket sock, String address) {
    if (hook != null) {
      hook.handlePostRegister(sock, address);
    }
  }

  /**
   * Client is unregistering a handler
   * @param sock The socket
   * @param address The address
   */
  protected boolean handleUnregister(SockJSSocket sock, String address) {
    if (hook != null) {
      return hook.handleUnregister(sock, address);
    }
    return true;
  }

  /**
   * Called before authorisation
   * You can use this to override default authorisation
   * @return true to handle authorisation yourself
   */
  protected boolean handleAuthorise(JsonObject message, final String sessionID,
                                    Handler<AsyncResult<Boolean>> handler) {
    if (hook != null) {
      return hook.handleAuthorise(message, sessionID, handler);
    } else {
      return false;
    }
  }

  private static final class PingInfo {
    long lastPing;
    long timerID;
  }

  private static final class SockInfo {
    int handlerCount;
    PingInfo pingInfo;
  }

  private void validatePermitted() {
    for (JsonObject obj: inboundPermitted) {
      validatePermitted(obj);
    }
    for (JsonObject obj: outboundPermitted) {
      validatePermitted(obj);
    }
  }

  private void validatePermitted(JsonObject obj) {
    for (String key: obj.fieldNames()) {
      boolean ok = key.equals("address") || key.equals("match") || key.equals("required_role")
        || key.equals("required_permission") || key.equals("address_re");
      if (!ok) {
        throw new IllegalArgumentException("Invalid field: " + key  + " in inbound/outbound permitted");
      }
    }
  }


}
