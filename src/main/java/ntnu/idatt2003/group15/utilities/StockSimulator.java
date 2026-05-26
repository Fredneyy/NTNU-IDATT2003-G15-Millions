package ntnu.idatt2003.group15.utilities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.stocks.Stock;

/**
 * Geometric Brownian Motion price simulator driven by {@link NewsItem}.
 *
 * <p>A baseline {@code NewsItem} per stock supplies the steady-state drift
 * ({@code item.drift()}) and volatility (raw σ). News headlines pushed through
 * {@code changePercent} and an elevated-volatility window that lasts
 * {@code durationUpdates} ticks with σ multiplied by {@code item.volatility()}.
 */
public class StockSimulator {

  private final Random random = new Random();
  private final double dt;

  /**
   * Instantiates a new Stock simulator.
   *
   * @param dt the dt of the simulation
   */
  public StockSimulator(double dt) {
    this.dt = dt;
  }

  /**
   * Stock-only tick: uses the stock's own {@code drift} and {@code volatility}.
   *
   * @param stock                the stock being ticked
   * @param volatilityMultiplier the volatility multiplier
   * @return the next stock price
   */
  public BigDecimal nextPrice(Stock stock, double volatilityMultiplier) {
    Objects.requireNonNull(stock, "stock cannot be null.");

    return gbmStep(stock.getSalesPrice(), stock.getDrift(),
        stock.getVolatility() * volatilityMultiplier);
  }

  /**
   * Sets the stock price to a product of it and the initial change.
   *
   * @param stock  to change price of
   * @param change the number to multiply stock price with
   * @return the product of the stock price and the change
   */
  public BigDecimal priceShock(Stock stock, BigDecimal change) {
    return stock.getSalesPrice().multiply(change);
  }

  private BigDecimal gbmStep(BigDecimal currentPrice, double drift, double volatility) {
    double z = random.nextGaussian();
    double exponent = (drift - 0.5 * volatility * volatility) * dt
        + (volatility * Math.sqrt(dt) * z);
    return currentPrice.multiply(BigDecimal.valueOf(Math.exp(exponent)));
  }
}
