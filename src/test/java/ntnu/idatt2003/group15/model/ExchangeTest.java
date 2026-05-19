package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTest {
  private Stock pgtStock;
  private Exchange exchange;
  private Share txShare;
  private Player player;


  @BeforeEach
  void setUp() {
    pgtStock = new Stock("PGT", "Porsgrunn toaletter", BigDecimal.valueOf(1000), 0.0, 0.0,
        List.of(StockSectors.INDUSTRIALS));
    List<Stock> stocks = List.of(
            new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(50), 0.0, 0.0,
                List.of(StockSectors.TECHNOLOGY)),
        pgtStock
      );
    exchange = new Exchange("FREX", stocks);
    txShare = new Share(pgtStock, BigDecimal.valueOf(2), BigDecimal.valueOf(1000));
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
      void sell () {
    }

      @Test
      void advance () {
    }

      @Test
      void getGainers () {
    }

      @Test
      void getLosers () {
    }
  }
}