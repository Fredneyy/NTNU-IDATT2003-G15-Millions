package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioControllerTest {

  private Stock stock;
  private Share share;
  private Portfolio portfolio;
  private PortfolioController controller;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    share = new Share(stock, BigDecimal.valueOf(100), stock.getSalesPrice());
    portfolio = new Portfolio();
    controller = new PortfolioController(portfolio);
  }

  @Test
  void listPropertyReturnsBackingObservableList() {
    assertSame(portfolio.getListProperty(), controller.getListProperty());
  }

  @Test
  void getSharesReflectsAddedShare() {
    portfolio.addShare(share);
    assertEquals(1, controller.getShares().size());
  }

  @Test
  void totalMarketValuePropertyReflectsCurrentValue() {
    portfolio.addShare(share);
    assertEquals(0,
        BigDecimal.valueOf(1000).compareTo(controller.totalMarketValueProperty().getValue()));
  }

  @Test
  void containsReturnsTrueForHeldShare() {
    portfolio.addShare(share);
    assertTrue(controller.contains(share));
  }

  @Test
  void containsReturnsFalseForUnheldShare() {
    Share other = new Share(stock, BigDecimal.valueOf(5), stock.getSalesPrice());
    assertFalse(controller.contains(other));
  }

  @Test
  void constructorRejectsNullPortfolio() {
    assertThrows(NullPointerException.class, () -> new PortfolioController(null));
  }
}
