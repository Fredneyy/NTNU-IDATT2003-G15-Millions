package ntnu.idatt2003.group15.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Smoke coverage for view classes — instantiate each and exercise its public
 * setters/getters so the constructor paths and update plumbing get loaded.
 *
 * Doesn't simulate user input; that requires TestFX + a usable headless toolkit
 * which TestFX's old Monocle build doesn't currently provide for JavaFX 25.
 */
class ViewSmokeTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void headerViewBuildsAndExposesButtons() {
    JavaFxTestSupport.runAndWait(() -> {
      HeaderView header = new HeaderView(() -> { /* exit no-op */ });
      // Buttons are created lazily inside createHeader().
      assertNotNull(header.createHeader());
      assertNotNull(header.getSettingsButton());
      assertNotNull(header.getSaveButton());
      assertNotNull(header.getAdvanceWeekButton());
      assertNotNull(header.getAutoAdvanceCheckBox());
      header.setPlayerName("Alice");
      header.setPlayerName(null); // null-tolerant
    });
  }

  @Test
  void newsContainerBuilds() {
    JavaFxTestSupport.runAndWait(() -> {
      NewsContainer container = new NewsContainer();
      assertNotNull(container.getView());
    });
  }

  @Test
  void newsFeedViewBuildsAndAcceptsEvents() {
    JavaFxTestSupport.runAndWait(() -> {
      NewsFeedView feed = new NewsFeedView();
      assertNotNull(feed.getView());

      ObservableList<NewsItem> events = FXCollections.observableArrayList();
      events.add(new NewsItem(
          "Tech surges",
          StockSectors.TECHNOLOGY,
          BigDecimal.ONE,
          BigDecimal.valueOf(0.05),
          3,
          Instant.now(),
          false));
      feed.setEvents(events);
      // Bindings.bindContent copies through — view's internal list now has 1.
    });
  }

  @Test
  void tabContainerBuildsWithEmptyAndConvenienceConstructor() {
    JavaFxTestSupport.runAndWait(() -> {
      TabContainer empty = new TabContainer();
      assertNotNull(empty.getView());

      TabContainer withTabs = new TabContainer(
          new TabContainer.Tab("a", "Alpha", null, new javafx.scene.layout.VBox()),
          new TabContainer.Tab("b", "Beta", null, new javafx.scene.layout.VBox()));
      assertNotNull(withTabs.getView());
    });
  }

  @Test
  void statisticsOverviewBuilds() {
    JavaFxTestSupport.runAndWait(() -> {
      StatisticsOverview overview = new StatisticsOverview();
      assertNotNull(overview.getView());
    });
  }

  @Test
  void tradesViewBuildsAndAcceptsRecords() {
    JavaFxTestSupport.runAndWait(() -> {
      TradesView trades = new TradesView();
      assertNotNull(trades.getView());
      assertEquals(0, trades.getTrades().size());

      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      TradesView.TradeRecord record = new TradesView.TradeRecord(
          TradesView.TradeType.BUY,
          apple,
          "AAPL", "Apple",
          BigDecimal.valueOf(5), BigDecimal.valueOf(100),
          Instant.now(),
          BigDecimal.ZERO);

      trades.setTrades(List.of(record));
      assertEquals(1, trades.getTrades().size());
      trades.setSearchFilter("AAPL");
      // Wiring the receipt callback after rows exist should be safe — rows
      // dispatch through TradesView's live field, not a captured snapshot.
      trades.setOnOpenReceipt(r -> { /* no-op */ });
    });
  }

  @Test
  void marketTableViewBuildsAndExposesRoot() {
    JavaFxTestSupport.runAndWait(() -> {
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      ObservableList<Stock> stocks = FXCollections.observableArrayList(apple);
      Portfolio portfolio = new Portfolio();
      PortfolioController portfolioController = new PortfolioController(portfolio);

      MarketTableView market = new MarketTableView(
          stocks, portfolioController,
          stock -> { /* onBuy */ },
          stock -> { /* onChart */ });

      assertNotNull(market.getView());
      // Adding a share later still drives the "owned" binding without throwing.
      portfolio.addShare(new Share(apple, BigDecimal.valueOf(2), BigDecimal.valueOf(100)));
      assertTrue(stocks.size() >= 1);
    });
  }

  @Test
  void portfolioTableViewBuildsAndExposesRoot() {
    JavaFxTestSupport.runAndWait(() -> {
      Portfolio portfolio = new Portfolio();
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      portfolio.addShare(new Share(apple, BigDecimal.valueOf(3), BigDecimal.valueOf(100)));
      PortfolioController controller = new PortfolioController(portfolio);

      PortfolioTableView table = new PortfolioTableView(
          controller,
          share -> { /* onSell */ },
          stock -> { /* onChart */ });

      assertNotNull(table.getView());
    });
  }
}
