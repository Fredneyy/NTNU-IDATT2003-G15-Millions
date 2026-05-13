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

  /** Active news-driven volatility window for a stock. Mutated in-place per tick. */
  private static final class ActiveNews {
    final double volatilityMultiplier;
    int remainingUpdates;

    ActiveNews(double volatilityMultiplier, int remainingUpdates) {
      this.volatilityMultiplier = volatilityMultiplier;
      this.remainingUpdates = remainingUpdates;
    }
  }

  private final Random random = new Random();
  private final double dt;
  private final Map<String, ActiveNews> activeNews = new HashMap<>();

  public StockSimulator(double dt) {
    this.dt = dt;
  }

  /**
   * Pure-function tick: GBM step using the news item's drift/volatility, with
   * no active-news overlay. Useful for tests and stateless callers.
   *
   * @param priceEvent   the news item supplying drift (changePercent / 100) and σ (volatility)
   * @param currentPrice the current price of the stock
   * @return the next stock price
   */
  public BigDecimal nextPrice(NewsItem priceEvent, BigDecimal currentPrice) {
    if (currentPrice == null) {
      throw new IllegalArgumentException("Current price cannot be null.");
    }
    if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Current price cannot be negative.");
    }
    Objects.requireNonNull(priceEvent, "priceEvent cannot be null.");

    double drift = driftOf(priceEvent);
    double volatility = volatilityOf(priceEvent);
    return gbmStep(currentPrice, drift, volatility);
  }

  /**
   * Stock-aware tick: like {@link #nextPrice(NewsItem, BigDecimal)} but consults
   * any active news effect registered for this stock's symbol. If a window is
   * active, σ is scaled by its multiplier and its remaining-updates counter is
   * decremented (and the entry dropped when it hits zero).
   *
   * @param baseline the baseline news item for this stock (drift / σ)
   * @param stock    the stock being ticked
   * @return the next stock price
   */
  public BigDecimal nextPrice(NewsItem baseline, Stock stock) {
    Objects.requireNonNull(baseline, "baseline cannot be null.");
    Objects.requireNonNull(stock, "stock cannot be null.");

    double volatility = volatilityOf(baseline);
    ActiveNews effect = activeNews.get(stock.getSymbol());
    if (effect != null) {
      volatility *= effect.volatilityMultiplier;
      effect.remainingUpdates--;
      if (effect.remainingUpdates <= 0) {
        activeNews.remove(stock.getSymbol());
      }
    }

    return gbmStep(stock.getSalesPrice(), driftOf(baseline), volatility);
  }

  /**
   * Stock-only tick: uses the stock's own {@code drift} and {@code volatility}
   * fields as the GBM baseline, with the same active-news σ overlay as
   * {@link #nextPrice(NewsItem, Stock)}.
   *
   * @param stock the stock being ticked
   * @return the next stock price
   */
  public BigDecimal nextPrice(Stock stock) {
    Objects.requireNonNull(stock, "stock cannot be null.");

    double volatility = stock.getVolatility();
    ActiveNews effect = activeNews.get(stock.getSymbol());
    if (effect != null) {
      volatility *= effect.volatilityMultiplier;
      effect.remainingUpdates--;
      if (effect.remainingUpdates <= 0) {
        activeNews.remove(stock.getSymbol());
      }
    }

    return gbmStep(stock.getSalesPrice(), stock.getDrift(), volatility);
  }

  /**
   * Apply a news item to every stock in the news item's sector: each affected
   * stock is shocked by {@code item.changePercent()} (pushed as a new sales
   * price) and registered with an elevated-volatility window of
   * {@code item.durationUpdates()} ticks scaled by {@code item.volatility()}.
   *
   * <p>If {@code item.sector()} is null, this is a no-op.
   */
  public void applyNews(NewsItem item, List<Stock> stocks) {
    Objects.requireNonNull(item, "news item cannot be null.");
    Objects.requireNonNull(stocks, "stocks cannot be null.");
    StockSectors sector = item.sector();
    if (sector == null) return;

    for (Stock stock : stocks) {
      if (stock.getCategories().contains(sector)) {
        shockAndQueue(item, stock);
      }
    }
  }

  private void shockAndQueue(NewsItem item, Stock stock) {
    BigDecimal changePercent = item.changePercent();
    if (changePercent != null && changePercent.signum() != 0) {
      BigDecimal factor = BigDecimal.ONE.add(changePercent.movePointLeft(2));
      BigDecimal shocked = stock.getSalesPrice().multiply(factor);
      if (shocked.signum() > 0) {
        stock.addNewSalesPrice(shocked);
      }
    }

    BigDecimal volMultiplier = item.volatility();
    if (volMultiplier != null && volMultiplier.signum() > 0 && item.durationUpdates() > 0) {
      activeNews.put(stock.getSymbol(),
          new ActiveNews(volMultiplier.doubleValue(), item.durationUpdates()));
    }
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
