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
   * Constructs a share representing an ownership at a fixed purchase price.
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

  /**
   * Returns the stock of the share.
   *
   * @return the stock
   */
  public Stock stock() {
    return stock;
  }

  /**
   * Returns the Quantity.
   *
   * @return the big decimal container the quantity of shares
   */
  public BigDecimal quantity() {
    return quantity;
  }

  /**
   * Returns the price per share.
   *
   * @return the big decimal containing the price per share
   */
  public BigDecimal pricePerShare() {
    return pricePerShare;
  }

  /**
   * Returns the quantity property object property.
   *
   * @return the object property containing the quantity of shares
   */
  public ObjectProperty<BigDecimal> quantityProperty() {
    return quantityProperty;
  }

  /**
   * Sell shares, reducing the held quantity.
   *
   * @param quantity the quantity to sell
   * @throws IllegalArgumentException if quantity exceeds held amount
   */
  public void sell(BigDecimal quantity) throws IllegalArgumentException {
    validateQuantity(quantity);
    setQuantity(this.quantity.subtract(quantity));
  }

  /**
   * Buy more shares, updating quantity and recalculating weighted average price.
   *
   * @param quantity      the quantity to buy
   * @param pricePerShare the price per share of the new purchase
   * @throws IllegalArgumentException if input is null or not positive in value
   */
  public void buy(BigDecimal quantity, BigDecimal pricePerShare) throws IllegalArgumentException {
    Objects.requireNonNull(pricePerShare, "Price per share cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException(
          "The price per share for " + stock.getSymbol()
              + " has to be greater than zero. You can't buy at a zero or negative price.");
    }
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException(
          "The number of shares to buy for " + stock.getSymbol()
              + " has to be greater than zero. Please enter a positive amount.");
    }
    BigDecimal newPricePerShare = this.quantity.multiply(this.pricePerShare)
        .add(quantity.multiply(pricePerShare))
        .divide(this.quantity.add(quantity), 10, RoundingMode.HALF_EVEN);
    setQuantity(this.quantity.add(quantity));
    setPricePerShare(newPricePerShare);
  }

  private void setQuantity(BigDecimal quantity) {
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException(
          "The share quantity for " + stock.getSymbol()
              + " has to be greater than zero. Please enter a positive number of shares.");
    }
    this.quantity = quantity;
    this.quantityProperty.set(quantity);
  }

  private void setPricePerShare(BigDecimal pricePerShare) {
    Objects.requireNonNull(pricePerShare, "Price per share cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException(
          "The price per share for " + stock.getSymbol() + " has to be greater than zero.");
    }
    this.pricePerShare = pricePerShare;
  }

  private void validateQuantity(BigDecimal quantity) {
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException(
          "The number of shares to sell for " + stock.getSymbol()
              + " has to be greater than zero. Please enter a positive amount.");
    }
    if (quantity.compareTo(this.quantity) > 0) {
      throw new IllegalArgumentException(
          "You're trying to sell " + quantity.toPlainString() + " shares of " + stock.getSymbol()
              + ", but you only own " + this.quantity.toPlainString() + ".");
    }
  }
}