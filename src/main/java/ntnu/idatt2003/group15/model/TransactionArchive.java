package ntnu.idatt2003.group15.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Maintains a historical ledger of all executed market transactions.
 */
public class TransactionArchive {
  private final ObservableList<Transaction> transactions;

  /**
   * Constructs a new  instance of a transaction archive.
   */
  public TransactionArchive() {
    transactions = FXCollections.observableArrayList();
  }

  /**
   * Add transaction to archive.
   *
   * @param transaction the transaction to add
   * @return {@code true} if added, {@code false} otherwise
   */
  public boolean add(Transaction transaction) {
    return transactions.add(transaction);
  }

  /**
   * Checks whether the archive is empty.
   *
   * @return {@code true} if empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  /**
   * Returns the transaction archive property.
   *
   * @return observable list of archive
   */
  public ObservableList<Transaction> getTransactionsProperty() {
    return transactions;
  }

  /**
   * Returns a list of transactions from a week.
   *
   * @param week the week to search for
   * @return a {@code List} containing found transactions
   */
  public List<Transaction> getTransactions(int week) {
    return transactions.stream().filter(transaction -> transaction.getWeek() == week).toList();
  }

  /**
   * Returns the purchases from a week.
   *
   * @param week the week to search for
   * @return a {@code List} containing every purchase that week
   */
  public List<Purchase> getPurchases(int week) {
    return transactions.stream().filter(Purchase.class::isInstance)
        .filter(transaction -> transaction.getWeek() == week).map(Purchase.class::cast).toList();
  }

  /**
   * Returns the sales from a week.
   *
   * @param week the week to search for
   * @return a {@code List} containing every sale that week
   */
  public List<Sale> getSales(int week) {
    return transactions.stream().filter(Sale.class::isInstance)
        .filter(transaction -> transaction.getWeek() == week).map(Sale.class::cast).toList();
  }

  /**
   * Returns amount of distinct weeks transactions are done.
   *
   * @return amount of weeks traded
   */
  public int countDistinctWeeks() {
    Set<Integer> distinctWeeks = transactions.stream()
        .map(Transaction::getWeek) // Get the week numbers
        .collect(Collectors.toSet()); // Collect into a Set for uniqueness

    return distinctWeeks.size();
  }
}
