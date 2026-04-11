package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;
import ntnu.idatt2003.group15.utilities.InputValidator;

/**
 * Represents an ownership stake in a company purchased at a specific price.
 */
public class Share {
  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal pricePerShare;

  /**
   * Constructs a share representing an ownership fraction at a fixed purchase price.
   *
   * @param stock the stock
   * @param quantity the amount of shares
   * @param pricePerShare the price per share
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal pricePerShare)
      throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(stock, "Stock cannot be null");
    if (!InputValidator.isBigDecimalValuePositive(quantity)) {
      throw new  IllegalArgumentException("Quantity must be positive");
    }
    if (!InputValidator.isBigDecimalValuePositive(pricePerShare)) {
      throw new  IllegalArgumentException("PricePerShare must be positive");
    }
    this.stock = stock;
    this.quantity = quantity;
    this.pricePerShare = pricePerShare;
  }

  /**
   * Returns the underlying stock entity this share is tied to.
   *
   * @return the stock
   */
  public Stock getStock() {
    return stock;
  }

  /**
   * Returns the amount of ownership fraction represented by this share.
   *
   * @return the amount of shares
   */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /**
   * Returns the original acquisition price strictly locked at the time of purchase.
   *
   * @return the price per share
   */
  public BigDecimal getPricePerShare() {
    return pricePerShare;
  }

}
