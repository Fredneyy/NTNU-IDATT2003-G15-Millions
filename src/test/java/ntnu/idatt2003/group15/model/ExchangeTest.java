package ntnu.idatt2003.group15.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExchangeTest {
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock pgtStock = new Stock("PGT", "Porsgrunn toaletter", BigDecimal.valueOf(1000), 0.0, 0.0,
        List.of(StockSectors.INDUSTRIALS));
    List<Stock> stocks = List.of(
            new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(50), 0.0, 0.0,
                List.of(StockSectors.TECHNOLOGY)),
        pgtStock
      );
    exchange = new Exchange("FREX", stocks);
    player = new Player("Ole Theodor", BigDecimal.valueOf(10000));
  }

  @Nested
  class positiveExchangeTests {

      @Test
      void getName () {
        assertEquals("FREX", exchange.getName());
      }

      @Test
      void getWeek () {
        assertEquals(1, exchange.getWeekProperty().get());
    }

      @Test
      void hasStock () {
        assertTrue(exchange.hasStock("AAPL"));
        assertFalse(exchange.hasStock("EQNR"));
    }

      @Test
      void getStock () {
        Stock porsgrunn = new Stock("PGT", "Porsgrunn toaletter", BigDecimal.valueOf(1000), 0.0, 0.0,
            List.of(StockSectors.INDUSTRIALS));
        assertEquals(porsgrunn, exchange.getStock("PGT"));
    }

      @Test
      void findStocks () {
        List<Stock> stocks = exchange.findStocks("Porsgrunn");
        assertEquals(stocks.getFirst(), exchange.getStock("PGT"));
    }

      @Test
      void buy () {
        exchange.buy("PGT", BigDecimal.valueOf(2), player);

        assertEquals(0, player.getMoney().compareTo(BigDecimal.valueOf(10000 - 2000)));
    }

      @Test
      void sellEntireHoldingAddsProceedsAfterCommissionAndTax () {
        // Buy at 1000/share so cost basis = 2000, then sell at the same price.
        // Gross proceeds = 2 * 1000 = 2000. Commission 1% of gross = 20.
        // Profit before tax = 2000 - 2000 cost basis - 20 commission = -20.
        // Negative taxable amount means no tax. Final net = 2000 - 20 = 1980.
        // Player started with 10_000, spent 2_000 on the buy, gains 1_980 on the sell.
        exchange.buy("PGT", BigDecimal.valueOf(2), player);
        Share held = player.getPortfolio().getShare("PGT");

        exchange.sell(held, BigDecimal.valueOf(2), player);

        assertEquals(0, player.getMoney().compareTo(BigDecimal.valueOf(10000 - 2000 + 1980)));
        assertFalse(player.getPortfolio().contains(held));
      }

      @Test
      void sellPartialHoldingLeavesRemainder () {
        exchange.buy("PGT", BigDecimal.valueOf(2), player);
        Share held = player.getPortfolio().getShare("PGT");

        exchange.sell(held, BigDecimal.valueOf(1), player);

        Share remaining = player.getPortfolio().getShare("PGT");
        assertEquals(0, BigDecimal.ONE.compareTo(remaining.quantity()));
      }

      @Test
      void advanceIncrementsWeekAndAppendsHistoricalPrice () {
        Stock apple = exchange.getStock("AAPL");
        int historySizeBefore = apple.getHistoricalPrices().size();
        int weekBefore = exchange.getWeekProperty().get();

        exchange.advance();

        assertEquals(weekBefore + 1, exchange.getWeekProperty().get());
        assertEquals(historySizeBefore + 1, apple.getHistoricalPrices().size());
      }

      @Test
      void getGainersOrdersByDescendingRelativeChange () {
        Stock apple = exchange.getStock("AAPL");
        Stock pgt = exchange.getStock("PGT");
        // AAPL: 50 -> 60 (+20%). PGT: 1000 -> 900 (-10%).
        apple.addNewSalesPrice(BigDecimal.valueOf(60));
        pgt.addNewSalesPrice(BigDecimal.valueOf(900));

        List<Stock> gainers = exchange.getGainers(2);

        assertEquals(2, gainers.size());
        assertEquals("AAPL", gainers.getFirst().getSymbol());
        assertEquals("PGT", gainers.get(1).getSymbol());
      }

      @Test
      void getGainersRespectsLimit () {
        exchange.getStock("AAPL").addNewSalesPrice(BigDecimal.valueOf(60));
        exchange.getStock("PGT").addNewSalesPrice(BigDecimal.valueOf(900));

        List<Stock> gainers = exchange.getGainers(1);

        assertEquals(1, gainers.size());
      }

      @Test
      void getLosersOrdersByAscendingRelativeChange () {
        Stock apple = exchange.getStock("AAPL");
        Stock pgt = exchange.getStock("PGT");
        apple.addNewSalesPrice(BigDecimal.valueOf(60));   // +20%
        pgt.addNewSalesPrice(BigDecimal.valueOf(900));    // -10%

        List<Stock> losers = exchange.getLosers(2);

        assertEquals("PGT", losers.getFirst().getSymbol());
        assertEquals("AAPL", losers.get(1).getSymbol());
      }

      @Test
      void getGainersAndLosersInvertEachOther () {
        exchange.getStock("AAPL").addNewSalesPrice(BigDecimal.valueOf(60));
        exchange.getStock("PGT").addNewSalesPrice(BigDecimal.valueOf(900));

        Stock topGainer = exchange.getGainers(1).getFirst();
        Stock topLoser = exchange.getLosers(1).getFirst();

        assertNotEquals(topGainer.getSymbol(), topLoser.getSymbol());
      }
  }
}
