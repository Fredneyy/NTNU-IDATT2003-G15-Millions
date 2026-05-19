package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import ntnu.idatt2003.group15.utilities.InputValidator;

/**
 * Represents an ownership stake in a company purchased at a specific price.
 */
public record Share(Stock stock, BigDecimal quantity, BigDecimal pricePerShare) {
  /**
   * Constructs a share representing an ownership fraction at a fixed purchase price.
   *
   * @param stock         the stock
   * @param quantity      the amount of shares
   * @param pricePerShare the price per share
   * @throws NullPointerException if any value is null
   * @throws IllegalArgumentException if quantity or price is negative or 0
   */
  public Share {
    Objects.requireNonNull(stock, "Stock cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    Objects.requireNonNull(pricePerShare, "Price per share cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException("PricePerShare must be positive");
    }
  }

  /**
   * Sell share. Generates a new share with quantity amount less
   *
   * @param quantity the quantity to subtract from the shares
   * @return the share with new quantity
   */
  public Share sell(BigDecimal quantity) {
    validateQuantity(quantity);
    return new Share(stock, this.quantity.subtract(quantity), pricePerShare);
  }

  public Share buy(BigDecimal quantity, BigDecimal pricePerShare) {
    validateQuantity(quantity);
    BigDecimal newPricePerShare = this.quantity.multiply(this.pricePerShare)
        .add(quantity.multiply(pricePerShare))
        .divide(this.quantity.add(quantity), 10, RoundingMode.HALF_EVEN);
    return new Share(stock, this.quantity.add(quantity), newPricePerShare);
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
