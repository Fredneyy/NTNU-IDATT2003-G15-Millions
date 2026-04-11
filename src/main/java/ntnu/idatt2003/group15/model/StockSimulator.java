package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Simulates market conditions and handles value fluctuations over time.
 */
public class StockSimulator {

  private final Random random = new Random();

  /**
   * Calculates the next stock price using the Geometric Brownian Motion model.
   *
   * @param currentPrice the current price of the stock
   * @param drift the drift to next price / expected return in a time period
   * @param volatility the volatility of the change
   * @param dt the time increment, measured in years
   * @return the next stock price
   */
  public BigDecimal nextPrice(BigDecimal currentPrice,
                              double drift, double volatility, double dt) {
    if (currentPrice == null) {
      throw new IllegalArgumentException("Current price cannot be null.");
    }
    if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Current price cannot be negative.");
    }
    if (volatility < 0) {
      throw new IllegalArgumentException("Volatility cannot be negative.");
    }
    if (dt < 0) {
      throw new IllegalArgumentException("Time increment (dt) cannot be negative.");
    }

    double z = random.nextGaussian();
    double exponent = (drift - 0.5 * Math.pow(volatility, 2)) * dt
        + (volatility * Math.sqrt(dt) * z);
    return currentPrice.multiply(BigDecimal.valueOf(Math.exp(exponent)));
  }
}
