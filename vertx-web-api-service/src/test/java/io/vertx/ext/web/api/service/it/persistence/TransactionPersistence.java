package io.vertx.ext.web.api.service.it.persistence;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import io.vertx.ext.web.api.service.it.models.Transaction;
import io.vertx.ext.web.api.service.it.persistence.impl.TransactionPersistenceImpl;

public interface TransactionPersistence {

  static TransactionPersistence create(){return new TransactionPersistenceImpl();
  }

  List<Transaction> getFilteredTransactions(Predicate<Transaction> predicate);

  Optional<Transaction> getTransaction (String transactionId);

  Transaction addTransaction(Transaction transaction);

  boolean updateTransaction(String transactionId, Transaction transaction);

  boolean removeTransaction(String transactionId);

}
