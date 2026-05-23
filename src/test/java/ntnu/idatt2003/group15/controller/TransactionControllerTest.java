package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionControllerTest {

  private Share share;
  private Purchase purchase;
  private Player player;
  private TransactionController controller;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    share = new Share(stock, BigDecimal.valueOf(5), BigDecimal.valueOf(100));
    purchase = new Purchase(share, 3);
    player = new Player("Trader", BigDecimal.valueOf(10000));
    controller = new TransactionController(purchase, player);
  }

  @Test
  void getShareReturnsTransactionShare() {
    assertSame(share, controller.getShare());
  }

  @Test
  void getWeekReturnsTransactionWeek() {
    assertEquals(3, controller.getWeek());
  }

  @Test
  void getCalculatorIsNotNull() {
    assertNotNull(controller.getCalculator());
  }

  @Test
  void isCommittedReturnsFalseBeforeCommit() {
    assertFalse(controller.isCommitted());
  }

  @Test
  void commitAppliesToPlayer() {
    controller.commit(BigDecimal.ZERO, BigDecimal.ZERO);
    assertTrue(player.getPortfolio().contains(share));
  }

  @Test
  void commitMarksTransactionAsCommitted() {
    controller.commit(BigDecimal.ZERO, BigDecimal.ZERO);
    assertTrue(controller.isCommitted());
  }

  @Test
  void commitRejectsNullCommission() {
    assertThrows(NullPointerException.class,
        () -> controller.commit(null, BigDecimal.ZERO));
  }

  @Test
  void commitRejectsNullTax() {
    assertThrows(NullPointerException.class,
        () -> controller.commit(BigDecimal.ZERO, null));
  }

  @Test
  void constructorRejectsNullTransaction() {
    assertThrows(NullPointerException.class,
        () -> new TransactionController(null, player));
  }

  @Test
  void constructorRejectsNullPlayer() {
    assertThrows(NullPointerException.class,
        () -> new TransactionController(purchase, null));
  }
}
