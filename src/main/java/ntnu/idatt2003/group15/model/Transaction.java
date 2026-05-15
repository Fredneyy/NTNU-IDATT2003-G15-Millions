package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Defines the base structure for financial operations within the market.
 */
public abstract class Transaction {
  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;
  private boolean committed;
  /** Wall-clock timestamp; defaults to creation time and survives save/load. */
  private Instant committedAt = Instant.now();

  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param share the share of the transaction
   * @param week the week of the transaction
   * @param calculator the transaction calculator
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(calculator, "Calculator cannot be null");

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
   *
   * @param committed the new state of the transaction
   */
  protected void setCommitted(boolean committed) {
    this.committed = committed;
  }

  /** Returns when this transaction was first instantiated (committed). */
  public Instant getCommittedAt() {
    return committedAt;
  }

  /** Restore a wall-clock timestamp (used when rebuilding from a save file). */
  protected void setCommittedAt(Instant committedAt) {
    this.committedAt = Objects.requireNonNull(committedAt, "committedAt");
  }

  /**
   * Commits a transaction.
   *
   * @param player the player
   * @param commission the commission of the transaction
   * @param tax the tax of the transaction
   */
  public abstract void commit(Player player, BigDecimal commission, BigDecimal tax);
}
