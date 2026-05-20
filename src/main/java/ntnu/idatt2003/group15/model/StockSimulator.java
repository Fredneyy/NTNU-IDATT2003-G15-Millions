package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

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

  public StockSimulator(double dt) {
    this.dt = dt;
  }

  /**
   * Stock-only tick: uses the stock's own {@code drift} and {@code volatility}
   *
   * @param stock the stock being ticked
   * @return the next stock price
   */
  public BigDecimal nextPrice(Stock stock) {
    Objects.requireNonNull(stock, "stock cannot be null.");

    return gbmStep(stock.getSalesPrice(), stock.getDrift(), stock.getVolatility());
  }

  private BigDecimal gbmStep(BigDecimal currentPrice, double drift, double volatility) {
    double z = random.nextGaussian();
    double exponent = (drift - 0.5 * volatility * volatility) * dt
        + (volatility * Math.sqrt(dt) * z);
    return currentPrice.multiply(BigDecimal.valueOf(Math.exp(exponent)));
  }

  private static double driftOf(NewsItem item) {
    BigDecimal d = item.drift();
    return d == null ? 0.0 : d.doubleValue();
  }

  private static double volatilityOf(NewsItem item) {
    BigDecimal v = item.volatility();
    return v == null ? 0.0 : v.doubleValue();
  }
}
