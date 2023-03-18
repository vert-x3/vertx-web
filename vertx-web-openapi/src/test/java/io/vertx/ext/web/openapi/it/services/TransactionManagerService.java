package io.vertx.ext.web.openapi.it.services;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.openapi.it.models.Transaction;
import io.vertx.ext.web.openapi.it.persistence.TransactionPersistence;
import io.vertx.ext.web.openapi.it.services.impl.TransactionManagerServiceImpl;

@WebApiServiceGen
public interface TransactionManagerService {

  static TransactionManagerService create (TransactionPersistence persistence) {
    return new TransactionManagerServiceImpl(persistence);

  }
  Future<ServiceResponse> getTransactionsList(
    List<String> from,
    List<String> to,
    List<String> message,
    ServiceRequest request);

  Future<ServiceResponse> getTransaction(
    String transactionId,
    ServiceRequest request);

  Future<ServiceResponse> createTransaction(
    Transaction body,
    ServiceRequest request);

  Future<ServiceResponse> updateTransaction(
    String transactionId,
    Transaction body,
    ServiceRequest request);

  Future<ServiceResponse> deleteTransaction(
    String transactionId,
    ServiceRequest request);
}
