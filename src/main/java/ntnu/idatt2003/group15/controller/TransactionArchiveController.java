package ntnu.idatt2003.group15.controller;

import java.util.List;
import java.util.Objects;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.transactions.Transaction;
import ntnu.idatt2003.group15.model.transactions.TransactionArchive;

public class TransactionArchiveController {

  private final TransactionArchive transactionArchive;

  public TransactionArchiveController(TransactionArchive transactionArchive) {
    this.transactionArchive = Objects.requireNonNull(transactionArchive,
        "Transaction archive cannot be null");
  }

  public boolean add(Transaction transaction) {
    return transactionArchive.add(transaction);
  }

  public boolean isEmpty() {
    return transactionArchive.isEmpty();
  }

  public ObservableList<Transaction> getTransactionsProperty() {
    return transactionArchive.getTransactionsProperty();
  }

  public List<Transaction> getTransactions(int week) {
    return transactionArchive.getTransactions(week);
  }

  public List<Purchase> getPurchases(int week) {
    return transactionArchive.getPurchases(week);
  }

  public List<Sale> getSales(int week) {
    return transactionArchive.getSales(week);
  }

  public int countDistinctWeeks() {
    return transactionArchive.countDistinctWeeks();
  }
}
