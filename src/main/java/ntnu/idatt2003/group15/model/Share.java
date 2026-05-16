package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
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
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public Share {
    Objects.requireNonNull(stock, "Stock cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new IllegalArgumentException("PricePerShare must be positive");
    }
  }
}
