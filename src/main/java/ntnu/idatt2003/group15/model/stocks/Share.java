package ntnu.idatt2003.group15.model.stocks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ntnu.idatt2003.group15.utilities.InputValidator;

/**
 * Represents an ownership stake in a company purchased at a specific price.
 */
public class Share {

  private final Stock stock;
  private BigDecimal quantity;
  private BigDecimal pricePerShare;

  private final ObjectProperty<BigDecimal> quantityProperty = new SimpleObjectProperty<>();

  /**
   * Constructs a share representing an ownership fraction at a fixed purchase price.
   *
   * @param stock         the stock
   * @param quantity      the amount of shares
   * @param pricePerShare the price per share
   * @throws NullPointerException     if any value is null
   * @throws IllegalArgumentException if quantity or price is negative or 0
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal pricePerShare) {
    Objects.requireNonNull(stock, "Stock cannot be null");
    this.stock = stock;
    setQuantity(quantity);
    setPricePerShare(pricePerShare);
  }

  public Stock stock() {
    return stock;
  }

  public BigDecimal quantity() {
    return quantity;
  }

  public BigDecimal pricePerShare() {
    return pricePerShare;
  }

  public ObjectProperty<BigDecimal> quantityProperty() {
    return quantityProperty;
  }

  private void setQuantity(BigDecimal quantity) {
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    this.quantity = quantity;
    this.quantityProperty.set(quantity);
  }

  private void setPricePerShare(BigDecimal pricePerShare) {
    Objects.requireNonNull(pricePerShare, "Price per share cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException("PricePerShare must be positive");
    }
    this.pricePerShare = pricePerShare;
  }

  /**
   * Sell shares, reducing the held quantity.
   *
   * @param quantity the quantity to sell
   * @throws IllegalArgumentException if quantity exceeds held amount
   */
  public void sell(BigDecimal quantity) {
    validateQuantity(quantity);
    setQuantity(this.quantity.subtract(quantity));
  }

  /**
   * Buy more shares, updating quantity and recalculating weighted average price.
   *
   * @param quantity      the quantity to buy
   * @param pricePerShare the price per share of the new purchase
   */
  public void buy(BigDecimal quantity, BigDecimal pricePerShare) {
    Objects.requireNonNull(pricePerShare, "Price per share cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException("PricePerShare must be positive");
    }
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    BigDecimal newPricePerShare = this.quantity.multiply(this.pricePerShare)
        .add(quantity.multiply(pricePerShare))
        .divide(this.quantity.add(quantity), 10, RoundingMode.HALF_EVEN);
    setQuantity(this.quantity.add(quantity));
    setPricePerShare(newPricePerShare);
  }

  private void validateQuantity(BigDecimal quantity) {
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (quantity.compareTo(this.quantity) > 0) {
      throw new IllegalArgumentException("Cannot sell more shares than owned");
    }
  }
}