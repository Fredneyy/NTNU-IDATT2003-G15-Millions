package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.Objects;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Transaction;
import ntnu.idatt2003.group15.model.TransactionCalculator;

/**
 * Controller mediating interactions between the UI and a single {@link Transaction}.
 * Delegates all business logic to the model layer on behalf of the active {@link Player}.
 */
public class TransactionController {

  private final Transaction transaction;
  private final Player player;

  /**
   * Constructs a controller bound to the given transaction and player.
   *
   * @param transaction the transaction to operate on
   * @param player      the player performing the transaction
   */
  public TransactionController(Transaction transaction, Player player) {
    this.transaction = Objects.requireNonNull(transaction, "Transaction cannot be null");
    this.player = Objects.requireNonNull(player, "Player cannot be null");
  }

  /**
   * Returns the share associated with the transaction.
   *
   * @return the share
   */
  public Share getShare() {
    return transaction.getShare();
  }

  /**
   * Returns the week in which the transaction takes place.
   *
   * @return the week number
   */
  public int getWeek() {
    return transaction.getWeek();
  }

  /**
   * Returns the calculator used to compute transaction amounts.
   *
   * @return the transaction calculator
   */
  public TransactionCalculator getCalculator() {
    return transaction.getCalculator();
  }

  /**
   * Returns whether the transaction has been committed.
   *
   * @return {@code true} if committed, {@code false} otherwise
   */
  public boolean isCommitted() {
    return transaction.isCommitted();
  }

  /**
   * Commits the transaction, applying the given commission and tax rates.
   *
   * @param commission the commission rate to apply
   * @param tax        the tax rate to apply
   * @throws NullPointerException if commission or tax is null
   */
  public void commit(BigDecimal commission, BigDecimal tax) {
    Objects.requireNonNull(commission, "Commission cannot be null");
    Objects.requireNonNull(tax, "Tax cannot be null");
    transaction.commit(player, commission, tax);
  }
}
