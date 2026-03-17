package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;

public class Sale extends Transaction {
  public Sale(Share share, int week, BigDecimal salesPrice) {
    super (share, week, new SaleCalculator(share, salesPrice));
  }

  public void commit(Player player) {
    BigDecimal saleAmount = getCalculator().calculateTotal();
    player.addMoney(saleAmount);
    player.getPortfolio().removeShare(getShare());
    player.getTransactionArchive().add(this);
  }
}
