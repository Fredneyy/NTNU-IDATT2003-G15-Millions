package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a buy transaction executed on the stock exchange.
 */
public class Purchase extends Transaction {
  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param share the share to purchase
   * @param week the week of the purchase
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator());
  }

  /**
   * Executes the  operation to manage market logic.
   *
   * @param player the player
   * @param commission the commission of the purchase
   * @param tax the tax of the purchase
   */
  @Override
  public void commit(Player player, BigDecimal commission, BigDecimal tax)
      throws NullPointerException {
    Objects.requireNonNull(player, "Player cannot be null");
    Objects.requireNonNull(commission, "Commission cannot be null");
    Objects.requireNonNull(tax, "Tax cannot be null");
    BigDecimal buyAmount = getCalculator().calculateTotal(getShare(), commission, tax);
    player.withdrawMoney(buyAmount);
    player.getPortfolio().addShare(getShare());
    player.getTransactionArchive().add(this);
    setCommitted(true);
  }
}
