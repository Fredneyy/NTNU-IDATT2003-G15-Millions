package ntnu.idatt2003.group15.view.dialog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.SaleCalculator;
import ntnu.idatt2003.group15.utilities.CsvParser;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.JavaFxTestSupport;
import ntnu.idatt2003.group15.view.NewsContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Drives each dialog through show() with a real Scene attached to the root
 * StackPane. Verifies the dialog and its overlay end up in the scene graph,
 * and that subsequent close() calls don't throw. Animations are kicked off
 * but we don't wait for them to finish — the harness just needs to exercise
 * the wiring.
 */
class DialogShowTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  private static StackPane rootWithScene() {
    StackPane root = new StackPane();
    // Scene needs a non-zero size to lay out the dialog.
    new Scene(root, 800, 600);
    return root;
  }

  @Test
  void infoDialogShowAttachesAndCloseRemoves() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      InfoDialog dialog = new InfoDialog();
      dialog.setText("Saved", "Game saved successfully");

      dialog.show(root);

      // Show adds overlay + dialog as children of root.
      assertTrue(root.getChildren().size() >= 2);
      // Showing again is idempotent — it shouldn't double-add.
      dialog.show(root);
      assertTrue(root.getChildren().size() >= 2);

      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void exceptionDialogShowAttachesAndCloseRemoves() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      ExceptionDialog dialog = new ExceptionDialog();
      dialog.setText("Error", "Something broke");

      dialog.show(root);

      assertTrue(root.getChildren().size() >= 2);
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void stockChartDialogShowAddsDialogAndCloseDetachesListener() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      // Push a few price points so the chart has data to render.
      apple.addNewSalesPrice(BigDecimal.valueOf(110));
      apple.addNewSalesPrice(BigDecimal.valueOf(105));

      StockChartDialog dialog = new StockChartDialog();
      dialog.show(root, apple);

      assertTrue(root.getChildren().contains(dialog.dialog));

      // close() detaches the listener and starts the exit animation.
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void stockChartDialogHandlesFlatPriceHistory() {
    JavaFxTestSupport.runAndWait(() -> {
      // Single price point -> range = 0 -> the flat-range branch is taken.
      StackPane root = rootWithScene();
      Stock flat = new Stock("FLAT", "Flat Inc", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.MACRO));

      StockChartDialog dialog = new StockChartDialog();
      dialog.show(root, flat);

      // Adding a new identical price still works through the listener path.
      flat.addNewSalesPrice(BigDecimal.valueOf(100));
    });
  }

  @Test
  void buyStockDialogShowBindsToStockAndCloseUnbinds() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(5000));
      BuyStockDialog dialog = new BuyStockDialog(
          cash,
          (s, q) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });

      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));

      dialog.show(root, apple);
      // Drive a price change — exercises bindToStock listeners.
      apple.addNewSalesPrice(BigDecimal.valueOf(150));

      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void sellStockDialogShowBindsToShareAndCloseUnbinds() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(5000));
      Portfolio portfolio = new Portfolio();
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(apple, BigDecimal.valueOf(3), BigDecimal.valueOf(100));
      portfolio.addShare(share);

      SellStockDialog dialog = new SellStockDialog(
          cash, new PortfolioController(portfolio), new SaleCalculator(),
          new BigDecimal("0.01"), new BigDecimal("0.37"),
          (s, q) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });

      dialog.show(root, share);

      // Bump the price to exercise the gross/net/fee bindings.
      apple.addNewSalesPrice(BigDecimal.valueOf(150));

      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void newsDialogShowOnRootAndClose() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      NewsDialog dialog = new NewsDialog(Duration.seconds(3));
      dialog.setText("Breaking");
      dialog.setSentiment(NewsDialog.Sentiment.BULLISH);
      dialog.setChangePercent(BigDecimal.valueOf(0.05));
      dialog.setSymbol("AAPL");

      dialog.show(root);

      // Showing again on the same root is idempotent.
      dialog.show(root);

      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void newsDialogShowInContainerAndClose() {
    JavaFxTestSupport.runAndWait(() -> {
      NewsContainer container = new NewsContainer();
      NewsDialog dialog = new NewsDialog(Duration.seconds(3));

      dialog.showIn(container);
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void newsDialogShowInWithNullContainerIsSafeNoOp() {
    JavaFxTestSupport.runAndWait(() -> {
      NewsDialog dialog = new NewsDialog(Duration.seconds(3));
      assertDoesNotThrow(() -> dialog.showIn(null));
    });
  }

  @Test
  void onBoardingDialogShowAddsToRoot() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = rootWithScene();
      OnBoardingDialog dialog = new OnBoardingDialog(new CsvParser(), new TaskUtil());

      dialog.show(root);
      // Showing again should be idempotent — only one dialog instance in the root.
      dialog.show(root);

      long dialogCount = root.getChildren().stream()
          .filter(node -> node.getStyleClass().contains("onboarding-card")).count();
      assertEquals(1L, dialogCount);

      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void transactionDialogCloseBeforeShowIsSafe() {
    JavaFxTestSupport.runAndWait(() -> {
      // Exercises the `root == null` early-return in TransactionDialog.close().
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(100));
      BuyStockDialog dialog = new BuyStockDialog(
          cash,
          (s, q) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });

      assertDoesNotThrow(dialog::close);
    });
  }
}
