package io.vertx.ext.web.openapi.it.persistence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertx.ext.web.openapi.it.models.Transaction;
import io.vertx.ext.web.openapi.it.persistence.TransactionPersistence;

public class TransactionPersistenceImpl implements TransactionPersistence {

  private final Map<String, Transaction> transactionMap;

  public TransactionPersistenceImpl() {
    transactionMap = new HashMap<>();
  }

  @Override
  public List<Transaction> getFilteredTransactions(Predicate<Transaction> predicate) {
    return transactionMap.values().stream().filter(predicate).collect(Collectors.toList());
  }

  @Override
  public Optional<Transaction> getTransaction(String transactionId) {
    return Optional.ofNullable(transactionMap.get(transactionId));
  }

  @Override
  public Transaction addTransaction(Transaction transaction) {
    transactionMap.put(transaction.getId(), transaction);
    return transaction;
  }

  @Override
  public boolean updateTransaction(String transactionId, Transaction transaction) {
    Transaction transactionToUpdate = transactionMap.put(transactionId, transaction);
    return transactionToUpdate != null;
  }

  @Override
  public boolean removeTransaction(String transactionId) {
    Transaction transactionToBeremoved = transactionMap.remove(transactionId);
    return transactionToBeremoved != null;
  }
}
