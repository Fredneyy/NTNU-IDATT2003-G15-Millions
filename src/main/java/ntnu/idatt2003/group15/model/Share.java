package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

import static ntnu.idatt2003.group15.utilities.InputValidator.isBigDecimalValuePositive;

public class Share {
  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) throws NullPointerException, IllegalArgumentException {
      Objects.requireNonNull(stock, "Stock cannot be null");
      isBigDecimalValuePositive("Quantity", quantity);
      isBigDecimalValuePositive("PurchasePrice", purchasePrice);
    this.stock = stock;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  public Stock getStock() {
    return stock;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

}
