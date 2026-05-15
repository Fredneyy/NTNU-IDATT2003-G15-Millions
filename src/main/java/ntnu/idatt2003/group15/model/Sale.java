package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a sell transaction executed on the stock exchange.
 */
public class Sale extends Transaction {
  /** Market price per share at the moment the sale committed (post-hoc immutable). */
  private BigDecimal salePricePerShare;
  /** Net proceeds the player actually received (after commission and tax). */
  private BigDecimal proceeds;

  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param share the share to sell
   * @param week the week of sale
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator());
  }

  /** Per-share market price at the moment of sale, or {@code null} if not yet committed. */
  public BigDecimal getSalePricePerShare() {
    return salePricePerShare;
  }

  /** Net proceeds the player received from this sale (gross minus commission and tax). */
  public BigDecimal getProceeds() {
    return proceeds;
  }

  /**
   * Realized profit/loss for this sale: {@code proceeds - costBasis}. The cost basis
   * is the original lot price × quantity (no buy-side fees are tracked).
   *
   * @return realized P/L, or {@code BigDecimal.ZERO} if not yet committed
   */
  public BigDecimal getRealizedPnl() {
    if (proceeds == null) return BigDecimal.ZERO;
    BigDecimal costBasis = getShare().getPricePerShare().multiply(getShare().getQuantity());
    return proceeds.subtract(costBasis);
  }

  /**
   * Executes the  operation to manage market logic.
   *
   * @param player the player that sells
   * @param commission the amount of commission
   * @param tax the amount of tax
   */
  @Override
  public void commit(Player player, BigDecimal commission, BigDecimal tax)
      throws NullPointerException {
    Objects.requireNonNull(player, "Player cannot be null");
    Objects.requireNonNull(commission, "Commission cannot be null");
    Objects.requireNonNull(tax, "Tax cannot be null");
    // Snapshot the live market price and net proceeds *before* the portfolio mutates,
    // so the trade ledger and realized-P/L calculations can rely on immutable values.
    this.salePricePerShare = getShare().getStock().getSalesPrice();
    this.proceeds = getCalculator().calculateTotal(getShare(), commission, tax);
    player.addMoney(this.proceeds);
    player.getPortfolio().removeShare(getShare());
    player.getTransactionArchive().add(this);
    setCommitted(true);
  }
}
