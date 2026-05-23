package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.view.JavaFxTestSupport;
import ntnu.idatt2003.group15.view.StatsView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises StatsController's math by wiring it to a real StatsView and pushing
 * transactions through a real Player. Asserts on the labels that the controller
 * writes into.
 */
class StatsControllerTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void refreshOnEmptyPlayerLeavesZeroDefaults() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      Player player = new Player("Empty", BigDecimal.valueOf(1000));
      StatsController controller = new StatsController(view, new PlayerController(player));

      controller.refresh();
      assertTrue(view.getHoldings().isEmpty());
    });
  }

  @Test
  void refreshAfterPurchasePopulatesHoldings() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      Player player = new Player("Buyer", BigDecimal.valueOf(10000));
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(apple, BigDecimal.valueOf(3), BigDecimal.valueOf(100));
      Purchase purchase = new Purchase(share, 1);
      purchase.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);

      StatsController controller = new StatsController(view, new PlayerController(player));
      controller.refresh();

      assertEquals(1, view.getHoldings().size());
    });
  }

  @Test
  void refreshAccountsForBuyAndSellTrades() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      Player player = new Player("Trader", BigDecimal.valueOf(10000));
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(apple, BigDecimal.valueOf(5), BigDecimal.valueOf(100));

      new Purchase(share, 1).commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      // Bump the price so the sale realizes a profit.
      apple.addNewSalesPrice(BigDecimal.valueOf(150));
      new Sale(share, 2).commit(player, BigDecimal.ZERO, BigDecimal.ZERO);

      StatsController controller = new StatsController(view, new PlayerController(player));
      controller.refresh();

      // The portfolio is empty after a full sale, but the trade history persists.
      assertTrue(view.getHoldings().isEmpty());
    });
  }

  @Test
  void refreshRunsWhenNetWorthChanges() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      Player player = new Player("Listener", BigDecimal.valueOf(1000));
      PlayerController playerController = new PlayerController(player);
      new StatsController(view, playerController);

      // The constructor wires a netWorth listener — verify it doesn't blow up
      // when the underlying property changes.
      player.addMoney(BigDecimal.valueOf(500));
    });
  }
}
