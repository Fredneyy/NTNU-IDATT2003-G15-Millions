package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;

public class Purchase extends Transaction {
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }

  public void commit(Player player) {
    BigDecimal buyAmount = getCalculator().calculateTotal();
    player.withdrawMoney(buyAmount);
    player.getPortfolio().addShare(getShare());
    player.getTransactionArchive().add(this);
  }
}
