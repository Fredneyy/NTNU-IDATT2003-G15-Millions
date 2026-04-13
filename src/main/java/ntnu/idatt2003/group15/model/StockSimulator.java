package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;

/**
 * Simulates market conditions and handles value fluctuations over time.
 */
public class StockSimulator {

  private final Random random = new Random();
  private final double dt;

  /**
   * Instantiates a new Stock simulator.
   *
   * @param dt the timestep for the simulation
   */
  public StockSimulator(double dt) {
    this.dt = dt;
  }

  /**
   * Calculates the next stock price using the Geometric Brownian Motion model.
   *
   * @param priceEvent   the object containing drift and volatility information
   * @param currentPrice the current price of the stock
   * @return the next stock price
   */
  public BigDecimal nextPrice(PriceEvent priceEvent, BigDecimal currentPrice) {
    if (currentPrice == null) {
      throw new IllegalArgumentException("Current price cannot be null.");
    }
    if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Current price cannot be negative.");
    }
    Objects.requireNonNull(priceEvent, "priceEvent cannot be null.");

    double z = random.nextGaussian();
    double exponent = (priceEvent.drift() - 0.5 * Math.pow(priceEvent.volatility(), 2)) * dt
        + (priceEvent.volatility() * Math.sqrt(dt) * z);
    return currentPrice.multiply(BigDecimal.valueOf(Math.exp(exponent)));
  }
}
