package ntnu.idatt2003.group15.view;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.*;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.utilities.SaveGameUtil;
import ntnu.idatt2003.group15.view.dialog.*;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

public class GameView {

  private static final Set<String> TABS_WITH_SEARCH = Set.of("market", "portfolio", "trades");

  private final StackPane view = new StackPane();
  private StackPane root;

  private final SettingsView settingsView;
  private final SettingsController settingsController;
  private final StatisticsOverview statisticsOverview = new StatisticsOverview();
  private final BuyStockDialog buyStockDialog;
  private final SellStockDialog sellStockDialog;
  private final StockChartDialog stockChartDialog = new StockChartDialog();
  private final MarketTableView marketTable;
  private final PortfolioTableView portfolioTable;
  private final NewsFeedView newsFeedView = new NewsFeedView();
  private final TextField searchField = new TextField();
  private final HBox searchBar;

  private final VBox marketContent;
  private final VBox portfolioContent;
  private final VBox tradesContent;
  private final ExchangeController exchangeController;
  private final PlayerController playerController;
  private final GameSettings gameSettings;
  private final Consumer<Throwable> errorHandler;
  private final InfoDialog infoDialog = new InfoDialog();

  private final TabContainer tabContainer;
  private ObservableList<NewsItem> news;
  private int unreadNews = 0;
  private final NewsContainer newsContainer = new NewsContainer();

  public GameView(PlayerController player, ExchangeController exchange,
                  GameSettings settings, Runnable runnableExit,
                  Consumer<Throwable> errorHandler, NewsController newsController) {
    this.playerController = Objects.requireNonNull(player);
    this.exchangeController = Objects.requireNonNull(exchange);
    this.news = Objects.requireNonNull(newsController.getNewsObservable());
    this.newsFeedView.setEvents(newsController.getNewsObservable());
    this.gameSettings = Objects.requireNonNull(settings, "settings");
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    ObservableList<Stock> marketStocks = FXCollections.observableArrayList();
    marketStocks.addAll(exchangeController.getAllStocks());

    buyStockDialog = new BuyStockDialog(
        exchangeController.cashProperty(),
        exchangeController::buy,
        errorHandler);

    PortfolioController portfolioController =
        new PortfolioController(playerController.getPortfolio());

    sellStockDialog = new SellStockDialog(
        exchangeController.cashProperty(),
        portfolioController,
        new SaleCalculator(),
        exchangeController.getCommission(),
        exchangeController.getTax(),
        exchangeController::sell,
        errorHandler);

    portfolioTable = new PortfolioTableView(portfolioController,
        share -> sellStockDialog.show(view, share),
        stock -> stockChartDialog.show(view, stock));
    portfolioContent = new VBox(portfolioTable.getView());

    marketTable = new MarketTableView(marketStocks,
        portfolioController,
        stock -> buyStockDialog.show(view, stock),
        stock -> stockChartDialog.show(view, stock));
    marketContent = new VBox(marketTable.getView());
    StatsView statsView = new StatsView();
    VBox statsContent = new VBox(statsView.getView());
    new StatsController(statsView, playerController, exchangeController);
    TradesView tradesView = new TradesView();
    tradesContent = new VBox(tradesView.getView());
    bindTradesView(tradesView);
    VBox newsContent = new VBox(newsFeedView.getView());
    tabContainer = new TabContainer(
        new TabContainer.Tab("market",    "Market",    FontAwesome.LINE_CHART,  marketContent),
        new TabContainer.Tab("portfolio", "Portfolio", FontAwesome.BRIEFCASE,   portfolioContent),
        new TabContainer.Tab("stats",     "Stats",     FontAwesome.BAR_CHART,   statsContent),
        new TabContainer.Tab("trades",    "Trades",    FontAwesome.CLOCK_O,     tradesContent),
        new TabContainer.Tab("news",      "News",      FontAwesome.NEWSPAPER_O, newsContent)
    );
    HeaderView headerView = new HeaderView(runnableExit);
    settingsView = new SettingsView(view);
    settingsController = new SettingsController(settingsView, gameSettings);
    headerView.setPlayerName(player.getName());
    view.getStylesheets().add(Objects.requireNonNull(
        getClass().getResource("/style/RootStyle.css")).toExternalForm());
    HBox header = headerView.createHeader();
    headerView.getSettingsButton().setOnAction(_ -> settingsView.toggle());
    headerView.getSaveButton().setOnAction(_ -> saveGame());

    addStatisticsCards();
    searchBar = buildSearchBar();
    styleTabContent(marketContent, portfolioContent, statsContent, tradesContent, newsContent);
    VBox.setVgrow(marketTable.getView(), Priority.ALWAYS);
    VBox.setVgrow(portfolioTable.getView(), Priority.ALWAYS);
    searchField.textProperty().addListener((_, _, q) -> {
      marketTable.setSearchFilter(q);
      portfolioTable.setSearchFilter(q);
      tradesView.setSearchFilter(q);
    });

    VBox.setVgrow(tabContainer.getView(), Priority.ALWAYS);

    settingsView.getView().getStyleClass().add("container");
    statisticsOverview.getView().getStyleClass().add("container");
    tabContainer.getView().getStyleClass().add("container");
    header.getStyleClass().add("container");

    wireSearchBarToTabs();

    VBox layout = new VBox();
    layout.getStyleClass().add("game-layout");
    layout.setFillWidth(false);
    layout.setAlignment(Pos.CENTER);
    layout.getChildren().addAll(
        header,
        settingsView.getView(),
        statisticsOverview.getView(),
        tabContainer.getView()
    );

    view.getStyleClass().add("game-view");
    ScrollPane scroll = new ScrollPane(layout);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.getStyleClass().add("game-scroll");
    view.getChildren().add(scroll);

    news.addListener((ListChangeListener<? super NewsItem>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          for (NewsItem item : c.getAddedSubList()) {
            onNewsEmitted(item);
          }
        }
      }
    });

    // Reset the News badge whenever the user actually opens the News tab.
    tabContainer.selectedTabProperty().addListener((_, _, sel) -> {
      if (sel != null && "news".equals(sel.getId())) clearNewsBadge();
    });
  }

  /**
   * Forward a freshly-emitted news item into the feed and bump the unread
   * counter on the News tab (unless that tab is already open).
   */
  public void onNewsEmitted(NewsItem item) {
    pushItem(item);
    TabContainer.Tab sel = tabContainer.selectedTabProperty().get();
    if (sel != null && "news".equals(sel.getId())) {
      return;
    }
    unreadNews++;
    tabContainer.setBadge("news", unreadNews > 99 ? "99+" : Integer.toString(unreadNews));
  }

  private void clearNewsBadge() {
    unreadNews = 0;
    tabContainer.setBadge("news", null);
  }

  public NewsFeedView getNewsFeedView() {
    return newsFeedView;
  }

  public SettingsController getSettingsController() {
    return settingsController;
  }


  public void show(StackPane root) {
    if (root != null && !root.getChildren().contains(view)) {
      this.root = root;
      root.getChildren().add(view);
      newsContainer.mountIn(root);
    }
  }


  public void close() {
    if (root != null) {
      root.getChildren().remove(view);
      newsContainer.unmount();
    }
  }

  private void saveGame() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Save Game");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Millions save file (*.json)", "*.json"));
    String name = playerController.getName();
    String suggested = (name == null || name.isBlank())
        ? "millions-save.json"
        : "millions-" + name.toLowerCase().replaceAll("\\s+", "-") + ".json";
    chooser.setInitialFileName(suggested);

    File target = chooser.showSaveDialog(view.getScene() == null ? null : view.getScene().getWindow());
    if (target == null) return; // user cancelled
    if (!target.getName().toLowerCase().endsWith(".json")) {
      target = new File(target.getParentFile(), target.getName() + ".json");
    }

    try {
      SaveGameUtil.save(target, playerController, exchangeController, gameSettings);
      showInfo("Game saved", "Saved to:\n" + target.getAbsolutePath());
    } catch (IOException ex) {
      showError("Could not save game", ex.getMessage());
    }
  }

  public void pushItem(NewsItem item) {
    NewsItem stamped = ensureStamped(item);

    NewsDialog dialog = new NewsDialog(Duration.seconds(10));
    if (stamped.changePercent().compareTo(BigDecimal.ZERO) > 0) {
      dialog.setSentiment(NewsDialog.Sentiment.BEARISH);
    } else {
      dialog.setSentiment(NewsDialog.Sentiment.BULLISH);
    }
    dialog.setSymbol(stamped.sector() == null ? null : stamped.sector().getLabel());
    BigDecimal changepercentFormatted = stamped.changePercent().subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
    dialog.setChangePercent(changepercentFormatted);
    dialog.setText(stamped.title(), stamped.message());
    dialog.setFooter(stamped.footerText());
    dialog.showIn(newsContainer);
  }

  private static NewsItem ensureStamped(NewsItem item) {
    if (item.when() != null) {
      return item;
    }
    return new NewsItem(
        item.sector(), item.changePercent(),
        item.title(), item.message(), item.volatility(),
        item.durationUpdates(), Instant.now(), item.appliedChange()
    );
  }

  private static void showInfo(String header, String message) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle("Millions");
    a.setHeaderText(header);
    a.setContentText(message);
    a.showAndWait();
  }

  private static void showError(String header, String message) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Millions");
    a.setHeaderText(header);
    a.setContentText(message);
    a.showAndWait();
  }

  /**
   * Wire the {@link TradesView} to the player's transaction archive so every
   * committed buy/sell appears in the ledger newest-first. Each transaction
   * carries its own {@code committedAt} timestamp (persisted to save files), so
   * "X ago" labels are stable across rebuilds and survive load.
   */
  private void bindTradesView(TradesView tradesView) {
    var archive = playerController.getTransactionArchive().getTransactionsProperty();

    Runnable rebuild = () -> {
      List<TradesView.TradeRecord> records = new ArrayList<>(archive.size());
      for (int i = archive.size() - 1; i >= 0; i--) {
        Transaction tx = archive.get(i);
        Instant when = tx.getCommittedAt() != null ? tx.getCommittedAt() : Instant.now();
        records.add(toRecord(tx, when));
      }
      tradesView.setTrades(records);
    };

    archive.addListener((ListChangeListener<Transaction>) _ -> rebuild.run());
    rebuild.run();
  }

  private static TradesView.TradeRecord toRecord(Transaction tx, Instant when) {
    boolean sell = tx instanceof Sale;
    TradesView.TradeType type = sell ? TradesView.TradeType.SELL : TradesView.TradeType.BUY;
    Stock stock = tx.getShare().stock();
    // For sells, prefer the actual sale price captured at commit time; the lot's
    // pricePerShare is the original *buy* price.
    BigDecimal price = tx.getShare().pricePerShare();
    if (sell) {
      BigDecimal sp = ((Sale) tx).getSalePricePerShare();
      if (sp != null) price = sp;
    }
    return new TradesView.TradeRecord(
        type,
        stock.getSymbol(),
        stock.getCompany(),
        tx.getShare().quantity(),
        price,
        when
    );
  }

  private void addStatisticsCards() {
    ObservableValue<BigDecimal> netWorth = exchangeController.netWorthProperty();
    ObservableValue<BigDecimal> cash = exchangeController.cashProperty();
    ObservableValue<BigDecimal> portfolioValue = exchangeController.portfolioValueProperty();
    ObservableValue<BigDecimal> pnl = exchangeController.unrealizedPnlProperty();

    statisticsOverview.addCard(new StatisticsOverview.StatCard(
        "netWorth", "Net Worth", FontAwesome.DOLLAR,
        money(netWorth)
    ));

    statisticsOverview.addCard(new StatisticsOverview.StatCard(
        "cash", "Cash Available", FontAwesome.MONEY,
        money(cash)
    ));

    statisticsOverview.addCard(new StatisticsOverview.StatCard(
        "portfolio", "Portfolio Value", FontAwesome.BULLSEYE,
        money(portfolioValue)
    ));

    statisticsOverview.addCard(new StatisticsOverview.StatCard(
        "pnl", "Unrealized P/L", FontAwesome.LINE_CHART,
        signedMoney(pnl)
    ));
    statisticsOverview.addCard(new StatisticsOverview.StatCard(
        "Player", "Status", FontAwesome.STAR,
        playerStatus(playerController.statusProperty())
    ));
  }

  private static ObservableValue<String> money(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatMoney(source.getValue()), source);
  }

  private static ObservableValue<String> signedMoney(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatSignedMoney(source.getValue()), source);
  }

  private static ObservableValue<String> playerStatus(ObservableValue<PlayerStatus> playerStatus) {
    return Bindings.createStringBinding(() -> formatPlayerStatus(playerStatus.getValue()), playerStatus);
  }

  private static String formatPlayerStatus(PlayerStatus value) {
    String stringValue = value.toString();
    String firstLetter = stringValue.substring(0, 1).toUpperCase();
    String restOfText = value.toString().substring(1).toLowerCase();
    return firstLetter.concat(restOfText);
  }

  private static String formatMoney(BigDecimal v) {
    if (v == null) return "$0.00";
    return "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static String formatSignedMoney(BigDecimal v) {
    if (v == null) return "+$0.00";
    String sign = v.signum() >= 0 ? "+" : "-";
    return sign + "$" + v.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private void wireSearchBarToTabs() {
    TabContainer.Tab initial = tabContainer.selectedTabProperty().get();
    if (initial != null && TABS_WITH_SEARCH.contains(initial.getId())) {
      placeSearchBarIn(targetContentFor(initial.getId()));
    }

    tabContainer.selectedTabProperty().addListener((_, _, newTab) -> {
      detachSearchBar();
      if (newTab != null && TABS_WITH_SEARCH.contains(newTab.getId())) {
        placeSearchBarIn(targetContentFor(newTab.getId()));
      }
    });
  }

  private VBox targetContentFor(String tabId) {
    return switch (tabId) {
      case "market" -> marketContent;
      case "portfolio" -> portfolioContent;
      case "trades" -> tradesContent;
      default -> null;
    };
  }

  private void placeSearchBarIn(VBox target) {
    if (target == null) return;
    if (!target.getChildren().contains(searchBar)) {
      target.getChildren().addFirst(searchBar);
    }
  }

  private void detachSearchBar() {
    Parent parent = searchBar.getParent();
    if (parent instanceof VBox vb) {
      vb.getChildren().remove(searchBar);
    }
  }

  private void styleTabContent(VBox... boxes) {
    for (VBox box : boxes) {
      box.getStyleClass().add("tab-content-pane");
      box.setSpacing(16);
    }
  }

  private HBox buildSearchBar() {
    FontIcon searchIcon = new FontIcon(FontAwesome.SEARCH);
    searchIcon.getStyleClass().add("search-icon");

    searchField.setPromptText("Search stocks by symbol or company name...");
    searchField.getStyleClass().add("search-field");
    HBox.setHgrow(searchField, Priority.ALWAYS);

    HBox box = new HBox(searchIcon, searchField);
    box.getStyleClass().add("search-bar");
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }
}