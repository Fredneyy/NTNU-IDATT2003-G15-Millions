package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Random;

public class StockSimulator {

  private final Random random = new Random();

  public BigDecimal nextPrice(BigDecimal currentPrice, double drift, double volatility, double dt) {
    double z = random.nextGaussian();
    double exponent = (drift - 0.5 * Math.pow(volatility, 2)) * dt
        + (volatility * Math.sqrt(dt) * z);
    return currentPrice.multiply(BigDecimal.valueOf(Math.exp(exponent)));
  }
}
