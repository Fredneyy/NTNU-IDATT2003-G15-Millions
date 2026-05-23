package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeControllerTest {

  private Stock apple;
  private Stock pgt;
  private Exchange exchange;
  private ExchangeController controller;
  private Player player;

  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(50), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    pgt = new Stock("PGT", "Porsgrunn toaletter", BigDecimal.valueOf(1000), 0.0, 0.0,
        List.of(StockSectors.INDUSTRIALS));
    exchange = new Exchange("FREX", List.of(apple, pgt));
    controller = new ExchangeController(exchange);
    player = new Player("Trader", BigDecimal.valueOf(10000));
  }

  @Test
  void getExchangeNameDelegates() {
    assertEquals("FREX", controller.getExchangeName());
  }

  @Test
  void getWeekDelegates() {
    assertEquals(1, controller.getWeek().get());
  }

  @Test
  void getAllStocksReturnsBothListedStocks() {
    assertEquals(2, controller.getAllStocks().size());
  }

  @Test
  void buyDecreasesPlayerCash() {
    controller.buy(pgt, BigDecimal.valueOf(2), player);
    assertEquals(0, BigDecimal.valueOf(8000).compareTo(player.getMoney()));
  }

  @Test
  void buyAddsShareToPortfolio() {
    controller.buy(pgt, BigDecimal.valueOf(2), player);
    assertTrue(player.getPortfolio().getShare("PGT") != null);
  }

  @Test
  void sellDelegatesToExchangeForOwnedShare() {
    controller.buy(pgt, BigDecimal.valueOf(2), player);
    Share held = player.getPortfolio().getShare("PGT");

    controller.sell(held, BigDecimal.valueOf(2), player);

    assertFalse(player.getPortfolio().contains(held));
  }

  @Test
  void sellRejectsShareNotOwnedByPlayer() {
    Share alien = new Share(apple, BigDecimal.valueOf(1), BigDecimal.valueOf(50));
    assertThrows(IllegalArgumentException.class,
        () -> controller.sell(alien, BigDecimal.valueOf(1), player));
  }

  @Test
  void sellRejectsNullShare() {
    assertThrows(NullPointerException.class,
        () -> controller.sell(null, BigDecimal.valueOf(1), player));
  }

  @Test
  void sellRejectsNullAmount() {
    controller.buy(pgt, BigDecimal.valueOf(2), player);
    Share held = player.getPortfolio().getShare("PGT");
    assertThrows(NullPointerException.class,
        () -> controller.sell(held, null, player));
  }

  @Test
  void getCommissionAndTaxExposeExchangeRates() {
    assertNotNull(controller.getCommission());
    assertNotNull(controller.getTax());
  }

  @Test
  void advanceWeekIncrementsExchangeWeek() {
    int before = controller.getWeek().get();
    controller.advanceWeek();
    assertEquals(before + 1, controller.getWeek().get());
  }

  @Test
  void setVolatilityMultiplierDoesNotThrow() {
    controller.setVolatilityMultiplier(1.5);
    // No observable side-effect to read back, but should not throw.
  }

  @Test
  void constructorRejectsNullExchange() {
    assertThrows(NullPointerException.class, () -> new ExchangeController(null));
  }
}
