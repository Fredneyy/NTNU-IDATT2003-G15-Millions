package ntnu.idatt2003.group15.model.transactions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;

/**
 * Defines the base structure for financial operations within the market.
 */
public abstract class Transaction {
  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;
  private boolean committed;
  private Instant committedAt = Instant.now();

  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param share      the share of the transaction
   * @param week       the week of the transaction
   * @param calculator the transaction calculator
   * @throws NullPointerException if any parameter is null
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(calculator, "TransactionCalculator cannot be null");

    this.share = share;
    this.week = week;
    this.calculator = calculator;
  }

  /**
   * Returns the share of the transaction.
   *
   * @return the share
   */
  public Share getShare() {
    return share;
  }

  /**
   * Returns the week of the transaction.
   *
   * @return the week
   */
  public int getWeek() {
    return week;
  }

  /**
   * Returns the calculator of the transaction.
   *
   * @return the calculator
   */
  public TransactionCalculator getCalculator() {
    return calculator;
  }

  /**
   * Checks whether the transaction is completed.
   *
   * @return {@code true} if completed, {@code false} otherwise
   */
  public boolean isCommitted() {
    return committed;
  }

  /**
   * Sets transaction to committed or not.
   */
  protected void setCommitted() {
    this.committed = true;
  }

  /**
   * Returns when this transaction was first instantiated (committed).
   *
   * @return  the committed at instant
   */
  public Instant getCommittedAt() {
    return committedAt;
  }

  /**
   * Restore a wall-clock timestamp (used when rebuilding from a save file).
   *
   * @param committedAt sets the this.committedAt to committedAt
   * @throws NullPointerException if any parameter is null
   */
  protected void setCommittedAt(Instant committedAt) throws NullPointerException {
    this.committedAt = Objects.requireNonNull(committedAt, "committedAt");
  }

  /**
   * Commits a transaction.
   *
   * @param player     the player
   * @param commission the commission of the transaction
   * @param tax        the tax of the transaction
   */
  public abstract void commit(Player player, BigDecimal commission, BigDecimal tax);
}
