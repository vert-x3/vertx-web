package io.vertx.ext.web.openapi.it.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.openapi.it.models.Transaction;
import io.vertx.ext.web.openapi.it.persistence.TransactionPersistence;
import io.vertx.ext.web.openapi.it.services.TransactionManagerService;

public class TransactionManagerServiceImpl implements TransactionManagerService {

  private final TransactionPersistence persistence;

  public TransactionManagerServiceImpl(TransactionPersistence persistence) {
    this.persistence = persistence;
  }
  @Override
  public void getTransactionsList(List<String> from, List<String> to, List<String> message, ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    List<Transaction> transactionsList = persistence.getFilteredTransactions(this.constructFilterPredicate(from, to, message));
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(
        new JsonArray(transactionsList.stream().map(Transaction::toJson).collect(Collectors.toList()))
      )
    ));
  }

  @Override
  public void getTransaction(String transactionId, ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    Optional<Transaction> t = persistence.getTransaction(transactionId);
    if (t.isPresent())
      resultHandler.handle(Future.succeededFuture(ServiceResponse.completedWithJson(t.get().toJson())));
    else
      resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusCode(404).setStatusMessage("Not Found")));
  }

  @Override
  public void createTransaction(Transaction body, ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    Transaction transactionAdded = persistence.addTransaction(body);
    resultHandler.handle(Future.succeededFuture(ServiceResponse.completedWithJson(transactionAdded.toJson())));
  }

  @Override
  public void updateTransaction(String transactionId, Transaction body, ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    if (persistence.updateTransaction(transactionId, body))
      resultHandler.handle(Future.succeededFuture(ServiceResponse.completedWithJson(body.toJson())));
    else
      resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusCode(404).setStatusMessage("Not Found")));
  }

  @Override
  public void deleteTransaction(String transactionId, ServiceRequest request, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    if (persistence.removeTransaction(transactionId))
      resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusCode(200).setStatusMessage("OK")));
    else
      resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusCode(404).setStatusMessage("Not Found")));
  }

  private Predicate<Transaction> constructFilterPredicate(List<String> from, List<String> to, List<String> message) {
    List<Predicate<Transaction>> predicateArrayList = new ArrayList<>();
    if(from != null) {
      predicateArrayList.add(transaction -> from.contains(transaction.getFrom()));
    }
    if (to != null) {
      predicateArrayList.add(transaction -> to.contains(transaction.getTo()));
    }
    if (message != null) {
      predicateArrayList.add(transaction -> message.stream().anyMatch(s -> s.contains(transaction.getMessage())));
    }
    return predicateArrayList.stream().reduce(transaction -> true, Predicate::and);
  }
}
