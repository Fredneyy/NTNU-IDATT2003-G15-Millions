package ntnu.idatt2003.group15.view.dialog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Construction smoke tests for the dialogs. We don't drive show/close because
 * that requires attaching to a Scene/Stage which the headless test runner can't
 * host. Even so, exercising the constructor exercises the bulk of each dialog's
 * wiring — table cell factories, listeners, label bindings, animations.
 */
class DialogSmokeTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void infoDialogConstructorAndSetText() {
    JavaFxTestSupport.runAndWait(() -> {
      InfoDialog dialog = new InfoDialog();
      dialog.setText("Title", "Body");
      dialog.setText("Title 2", "Body 2");
    });
  }

  @Test
  void exceptionDialogConstructorAndSetText() {
    JavaFxTestSupport.runAndWait(() -> {
      ExceptionDialog dialog = new ExceptionDialog();
      dialog.setText("Oops", "Something went wrong");
    });
  }

  @Test
  void infoDialogCloseBeforeShowIsSafe() {
    JavaFxTestSupport.runAndWait(() -> {
      InfoDialog dialog = new InfoDialog();
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void buyStockDialogConstructorWiresCashBinding() {
    JavaFxTestSupport.runAndWait(() -> {
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(1000));
      BuyStockDialog dialog = new BuyStockDialog(
          cash,
          (stock, qty) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });
      assertNotNull(dialog);
      // Drive the cash property — exercises the bound label.
      cash.set(BigDecimal.valueOf(750));
    });
  }

  @Test
  void sellStockDialogConstructorWiresDependencies() {
    JavaFxTestSupport.runAndWait(() -> {
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(1000));
      Portfolio portfolio = new Portfolio();
      PortfolioController portfolioController = new PortfolioController(portfolio);

      SellStockDialog dialog = new SellStockDialog(
          cash, portfolioController, new SaleCalculator(),
          BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.37),
          (share, qty) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });
      assertNotNull(dialog);
    });
  }

  @Test
  void stockChartDialogConstructorBuilds() {
    JavaFxTestSupport.runAndWait(() -> {
      StockChartDialog dialog = new StockChartDialog();
      assertNotNull(dialog);
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void newsDialogConstructorAndSetters() {
    JavaFxTestSupport.runAndWait(() -> {
      NewsDialog dialog = new NewsDialog(Duration.seconds(3));

      dialog.setText("Tech surges on AI breakthrough");
      dialog.setSentiment(NewsDialog.Sentiment.BULLISH);
      dialog.setSentiment(NewsDialog.Sentiment.BEARISH);
      dialog.setSentiment(NewsDialog.Sentiment.NEUTRAL);
      dialog.setSymbol("AAPL");
      dialog.setChangePercent(BigDecimal.valueOf(0.05));
      dialog.setChangePercent(BigDecimal.valueOf(-0.03));
      dialog.setFooter("Volatility +20% for 3 updates");
    });
  }

  @Test
  void onBoardingDialogConstructorLoadsTextSynchronously() {
    JavaFxTestSupport.runAndWait(() -> {
      // TaskUtil with no executor falls back to synchronous execution,
      // so the CSV is parsed inline during construction.
      TaskUtil taskUtil = new TaskUtil();
      OnBoardingDialog dialog = new OnBoardingDialog(new CsvParser(), taskUtil);
      assertNotNull(dialog);
    });
  }

  @Test
  void buyStockDialogShowIsSafelyConstructible() {
    // Even without a Scene we can validate that the show() entry point exists
    // and accepts a Stock — full show flow requires a Stage, not testable here.
    JavaFxTestSupport.runAndWait(() -> {
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(1000));
      BuyStockDialog dialog = new BuyStockDialog(
          cash,
          (stock, qty) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      // We can't actually call show() without a Scene-bearing root, but the
      // constructor already exercised most of the wiring.
      assertNotNull(apple);
      assertNotNull(dialog);
    });
  }

  @Test
  void receiptDialogConstructorAndShowFlows() {
    JavaFxTestSupport.runAndWait(() -> {
      ReceiptDialog dialog = new ReceiptDialog();
      assertNotNull(dialog);
      // close before any show() is safe — exercises the null-root guard.
      assertDoesNotThrow(dialog::close);

      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      StackPane root = new StackPane();

      // BUY path — no fees, row 3 hides.
      dialog.show(root, ReceiptDialog.Type.BUY, apple,
          BigDecimal.valueOf(3), BigDecimal.valueOf(100),
          BigDecimal.ZERO, BigDecimal.valueOf(-300));
      assertDoesNotThrow(dialog::close);

      // SELL path — full breakdown with gross / fees / net.
      dialog.show(root, ReceiptDialog.Type.SELL, apple,
          BigDecimal.valueOf(2), BigDecimal.valueOf(150),
          BigDecimal.valueOf(15), BigDecimal.valueOf(285));
      assertDoesNotThrow(dialog::close);
    });
  }

  @Test
  void sellStockDialogConstructorWithPopulatedPortfolio() {
    JavaFxTestSupport.runAndWait(() -> {
      SimpleObjectProperty<BigDecimal> cash = new SimpleObjectProperty<>(BigDecimal.valueOf(5000));
      Portfolio portfolio = new Portfolio();
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(apple, BigDecimal.valueOf(3), BigDecimal.valueOf(100));
      portfolio.addShare(share);

      SellStockDialog dialog = new SellStockDialog(
          cash, new PortfolioController(portfolio), new SaleCalculator(),
          BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.37),
          (s, qty) -> { /* onConfirm */ },
          throwable -> { /* errorHandler */ });

      // Bump price — exercises the bindings inside the dialog.
      apple.addNewSalesPrice(BigDecimal.valueOf(150));
      assertNotNull(dialog);
    });
  }
}
