package ntnu.idatt2003.group15.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.news.NewsItem;
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
      void getAllStocksReturnsImmutableSnapshotOfListedStocks () {
        List<Stock> all = exchange.getAllStocks();
        assertEquals(2, all.size());
        assertThrows(UnsupportedOperationException.class, () -> all.add(
            new Stock("X", "X", BigDecimal.ONE, 0.0, 0.0, List.of(StockSectors.MACRO))));
      }

      @Test
      void getCommissionReturnsConfiguredRate () {
        assertEquals(0, new BigDecimal("0.01").compareTo(exchange.getCommission()));
      }

      @Test
      void getTaxReturnsConfiguredRate () {
        assertEquals(0, new BigDecimal("0.37").compareTo(exchange.getTax()));
      }

      @Test
      void setWeekRestoresExchangeWeek () {
        exchange.setWeek(42);
        assertEquals(42, exchange.getWeekProperty().get());
      }

      @Test
      void setVolatilityMultiplierAffectsAdvanceWithoutThrowing () {
        exchange.setVolatilityMultiplier(0.0);
        exchange.advance();
        assertNotNull(exchange.getStock("AAPL").getSalesPrice());
      }

      @Test
      void advanceConsumesActiveNewsAndMarksAppliedChange () {
        NewsItem techNews = new NewsItem(
            "Tech surges", StockSectors.TECHNOLOGY,
            BigDecimal.valueOf(1.5), BigDecimal.valueOf(1.10), 3, Instant.now(), false);
        ObservableList<NewsItem> news = FXCollections.observableArrayList(techNews);
        exchange.setNewsObservableList(news);

        exchange.advance();

        assertTrue(techNews.appliedChange());
      }

      @Test
      void advanceIgnoresExpiredNewsItems () {
        NewsItem expired = new NewsItem(
            "Old news", StockSectors.TECHNOLOGY,
            BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), 0, Instant.now(), false);
        ObservableList<NewsItem> news = FXCollections.observableArrayList(expired);
        exchange.setNewsObservableList(news);

        exchange.advance();

        assertEquals(0, expired.durationUpdates());
      }

      @Test
      void setNewsObservableListRejectsNull () {
        assertThrows(NullPointerException.class, () -> exchange.setNewsObservableList(null));
      }
  }

  @Nested
  class negativeExchangeTests {

    @Test
    void constructorRejectsNullName() {
      assertThrows(NullPointerException.class, () -> new Exchange(null, List.of()));
    }

    @Test
    void constructorRejectsBlankName() {
      assertThrows(BlankArgumentException.class, () -> new Exchange("   ", List.of()));
    }

    @Test
    void constructorRejectsNullStocks() {
      assertThrows(NullPointerException.class, () -> new Exchange("FREX", null));
    }
  }
}
