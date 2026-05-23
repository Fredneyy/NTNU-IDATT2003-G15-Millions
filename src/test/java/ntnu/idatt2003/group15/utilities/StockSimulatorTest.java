package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StockSimulatorTest {

  private StockSimulator simulator;
  private Stock stock;

  @BeforeEach
  void setUp() {
    simulator = new StockSimulator(1.0 / 52.0);
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
  }

  @Nested
  @DisplayName("Positive StockSimulator Tests")
  class positiveStockSimulatorTests {

    @Test
    void priceShockMultipliesCurrentSalesPriceByFactor() {
      BigDecimal result = simulator.priceShock(stock, BigDecimal.valueOf(1.5));
      assertEquals(0, BigDecimal.valueOf(150).compareTo(result));
    }

    @Test
    void priceShockWithUnitFactorIsUnchanged() {
      BigDecimal result = simulator.priceShock(stock, BigDecimal.ONE);
      assertEquals(0, stock.getSalesPrice().compareTo(result));
    }

    @Test
    void nextPriceWithZeroVolatilityAndZeroDriftReturnsCurrentPrice() {
      // With drift = 0 and volatility = 0, the GBM exponent collapses to 0,
      // so exp(0) = 1 and the next price equals the current price.
      BigDecimal result = simulator.nextPrice(stock, 1.0);
      assertEquals(0, stock.getSalesPrice().compareTo(result));
    }
  }

  @Nested
  @DisplayName("Negative StockSimulator Tests")
  class negativeStockSimulatorTests {

    @Test
    void nextPriceWithNullStockThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () -> simulator.nextPrice(null, 1.0));
    }
  }
}
