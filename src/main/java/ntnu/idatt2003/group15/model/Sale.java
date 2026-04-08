package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a sell transaction executed on the stock exchange.
 */
public class Sale extends Transaction {
  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param share the share to sell
   * @param week the week of sale
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator());
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
    BigDecimal saleAmount = getCalculator().calculateTotal(getShare(), commission, tax);
    player.addMoney(saleAmount);
    player.getPortfolio().removeShare(getShare());
    player.getTransactionArchive().add(this);
    setCommitted(true);
  }
}
