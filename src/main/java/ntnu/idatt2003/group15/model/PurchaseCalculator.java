package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;

public class PurchaseCalculator implements TransactionCalculator {
  private BigDecimal purchasePrice;
  private BigDecimal quantity;

  public PurchaseCalculator(Share share) {
      this.purchasePrice = share.getPurchasePrice();
      this.quantity = share.getQuantity();
  }

  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  public BigDecimal calculateCommission() {
    return calculateGross().multiply(BigDecimal.valueOf(0.005)); // 0.5%
  }

  public BigDecimal calculateTax() {
    return BigDecimal.valueOf(0);
  }

  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateCommission()).add(calculateTax());
  }
}
