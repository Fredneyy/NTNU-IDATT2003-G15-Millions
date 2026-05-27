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

import javafx.beans.Observable;
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
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.player.PlayerStatus;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.transactions.SaleCalculator;
import ntnu.idatt2003.group15.model.transactions.Transaction;
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
  private final ReceiptDialog receiptDialog = new ReceiptDialog();
  private final ConfirmDialog confirmDialog = new ConfirmDialog();
  private final MarketTableView marketTable;
  private final PortfolioTableView portfolioTable;
  private final TradesView tradesView = new TradesView();
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
  private int unreadNews = 0;
  private final NewsContainer newsContainer = new NewsContainer();

  public GameView(PlayerController player, ExchangeController exchange,
                  GameSettings settings, Runnable runnableExit,
                  Consumer<Throwable> errorHandler, NewsController newsController, Runnable advance,
                  Runnable autoAdvanceOn, Runnable autoAdvanceOff) {
    this.playerController = Objects.requireNonNull(player);
    this.exchangeController = Objects.requireNonNull(exchange);
    this.gameSettings = Objects.requireNonNull(settings, "settings");
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");

    ObservableList<NewsItem> news = Objects.requireNonNull(newsController.getNewsObservable());
    this.newsFeedView.setEvents(news);

    PortfolioController portfolioController =
        new PortfolioController(playerController.getPortfolio());
    SaleCalculator saleCalculator = new SaleCalculator();

    buyStockDialog = createBuyStockDialog();
    sellStockDialog = createSellStockDialog(portfolioController, saleCalculator);

    portfolioTable = new PortfolioTableView(portfolioController,
        share -> sellStockDialog.show(view, share),
        stock -> stockChartDialog.show(view, stock));
    portfolioContent = createPortfolioContent(portfolioController);

    marketTable = new MarketTableView(createMarketStocks(),
        portfolioController,
        stock -> buyStockDialog.show(view, stock),
        stock -> stockChartDialog.show(view, stock));
    marketContent = new VBox(marketTable.getView());

    VBox statsContent = createStatsContent();
    tradesContent = createTradesContent();
    VBox newsContent = new VBox(newsFeedView.getView());

    tabContainer = createTabContainer(
        marketContent, portfolioContent, statsContent, tradesContent, newsContent);

    HeaderView headerView = new HeaderView(createExitWithConfirmation(runnableExit));
    settingsView = new SettingsView(view);
    settingsController = new SettingsController(settingsView, gameSettings);

    view.getStylesheets().add(Objects.requireNonNull(
        getClass().getResource("/style/RootStyle.css")).toExternalForm());

    HBox header = buildHeader(headerView, advance, autoAdvanceOn, autoAdvanceOff);

    addStatisticsCards();
    searchBar = buildSearchBar();
    styleTabContent(marketContent, portfolioContent, statsContent, tradesContent, newsContent);
    VBox.setVgrow(marketTable.getView(), Priority.ALWAYS);
    VBox.setVgrow(portfolioTable.getView(), Priority.ALWAYS);
    VBox.setVgrow(tabContainer.getView(), Priority.ALWAYS);

    wireSearchFilter();
    applyContainerStyles(header);
    wireSearchBarToTabs();
    assembleScrollableLayout(header);

    wireNewsListener(news);
    wireTabChangeListener();
  }

  private ObservableList<Stock> createMarketStocks() {
    ObservableList<Stock> stocks = FXCollections.observableArrayList(
        stock -> new Observable[] { stock.getHistoricalPrices() }
    );
    stocks.addAll(exchangeController.getAllStocks());
    return stocks;
  }

  private BuyStockDialog createBuyStockDialog() {
    return new BuyStockDialog(
        playerController.getCashProperty(),
        (Stock stock, BigDecimal quantity) -> {
          BigDecimal price = stock.getSalesPrice();
          exchangeController.buy(stock, quantity, playerController.getPlayer());
          BigDecimal gross = price.multiply(quantity);
          receiptDialog.show(view, ReceiptDialog.Type.BUY, stock, quantity, price,
              BigDecimal.ZERO, gross.negate(), Instant.now());
        },
        errorHandler);
  }

  private SellStockDialog createSellStockDialog(PortfolioController portfolioController,
                                                SaleCalculator saleCalculator) {
    return new SellStockDialog(
        playerController.getCashProperty(),
        portfolioController,
        saleCalculator,
        exchangeController.getCommission(),
        exchangeController.getTax(),
        (Share share, BigDecimal amount) -> {
          BigDecimal salePrice = share.stock().getSalesPrice();
          BigDecimal soldQty = amount.min(share.quantity());
          Share soldLot = new Share(share.stock(), soldQty, share.pricePerShare());
          BigDecimal gross = saleCalculator.calculateGross(soldLot);
          BigDecimal net = saleCalculator.calculateTotal(
              soldLot, exchangeController.getCommission(), exchangeController.getTax());
          BigDecimal fees = gross.subtract(net);
          exchangeController.sell(share, amount, playerController.getPlayer());
          receiptDialog.show(view, ReceiptDialog.Type.SELL, share.stock(), soldQty,
              salePrice, fees, net, Instant.now());
        },
        errorHandler);
  }

  private VBox createPortfolioContent(PortfolioController portfolioController) {
    SellAllCard sellAllCard = new SellAllCard(
        portfolioController.getListProperty(),
        () -> confirmDialog.show(view,
            "Sell all stocks?",
            "This will liquidate every share you "
            + "currently hold. Do you really want to sell all stocks?",
            "Sell all",
            "Cancel",
            this::sellAllStocks));
    return new VBox(portfolioTable.getView(), sellAllCard.getView());
  }

  private VBox createStatsContent() {
    StatsView statsView = new StatsView();
    new StatsController(statsView, playerController);
    return new VBox(statsView.getView());
  }

  private VBox createTradesContent() {
    VBox content = new VBox(tradesView.getView());
    bindTradesView(tradesView);
    tradesView.setOnOpenReceipt(record -> {
      ReceiptDialog.Type type = record.type() == TradesView.TradeType.BUY
          ? ReceiptDialog.Type.BUY
          : ReceiptDialog.Type.SELL;
      BigDecimal net = record.net();
      receiptDialog.show(view, type, record.stock(),
          record.quantity(), record.price(), record.fees(), net, record.when());
    });
    return content;
  }

  private TabContainer createTabContainer(VBox marketContent, VBox portfolioContent,
                                          VBox statsContent, VBox tradesContent,
                                          VBox newsContent) {
    return new TabContainer(
        new TabContainer.Tab("market",    "Market",    FontAwesome.LINE_CHART,  marketContent),
        new TabContainer.Tab("portfolio", "Portfolio", FontAwesome.BRIEFCASE,   portfolioContent),
        new TabContainer.Tab("stats",     "Stats",     FontAwesome.BAR_CHART,   statsContent),
        new TabContainer.Tab("trades",    "Trades",    FontAwesome.CLOCK_O,     tradesContent),
        new TabContainer.Tab("news",      "News",      FontAwesome.NEWSPAPER_O, newsContent)
    );
  }

  private Runnable createExitWithConfirmation(Runnable runnableExit) {
    return () -> confirmDialog.show(
        view,
        "Save before exiting?",
        "You're about to leave the game. Would you like to save your progress first?",
        "Exit without saving",
        "Save and exit",
        "Cancel",
        runnableExit,
        () -> {
          if (saveGame()) {
            runnableExit.run();
          }
        });
  }

  private HBox buildHeader(HeaderView headerView, Runnable advance,
                           Runnable autoAdvanceOn, Runnable autoAdvanceOff) {
    headerView.setPlayerName(playerController.getName());
    HBox header = headerView.createHeader();
    headerView.getAutoAdvanceCheckBox().selectedProperty().addListener((value, _, _) -> {
      if (value.getValue()) {
        autoAdvanceOn.run();
      } else {
        autoAdvanceOff.run();
      }
    });
    headerView.getAdvanceWeekButton().setOnAction(_ -> advance.run());
    headerView.getSettingsButton().setOnAction(_ -> settingsView.toggle());
    headerView.getSaveButton().setOnAction(_ -> saveGame());
    return header;
  }

  private void wireSearchFilter() {
    searchField.textProperty().addListener((_, _, q) -> {
      marketTable.setSearchFilter(q);
      portfolioTable.setSearchFilter(q);
      tradesView.setSearchFilter(q);
    });
  }

  private void applyContainerStyles(HBox header) {
    settingsView.getView().getStyleClass().add("container");
    statisticsOverview.getView().getStyleClass().add("container");
    tabContainer.getView().getStyleClass().add("container");
    header.getStyleClass().add("container");
  }

  private void assembleScrollableLayout(HBox header) {
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
    scroll.setFitToHeight(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.getStyleClass().add("game-scroll");
    view.getChildren().add(scroll);
  }

  private void wireNewsListener(ObservableList<NewsItem> news) {
    news.addListener((ListChangeListener<? super NewsItem>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          for (NewsItem item : c.getAddedSubList()) {
            onNewsEmitted(item);
          }
        }
      }
    });
  }

  private void wireTabChangeListener() {
    tabContainer.selectedTabProperty().addListener((_, _, sel) -> {
      if (sel != null && "news".equals(sel.getId())) {
        clearNewsBadge();
      }
    });
  }

  public void onNewsEmitted(NewsItem item) {
    pushItem(item);
    TabContainer.Tab sel = tabContainer.selectedTabProperty().get();
    if (sel != null && "news".equals(sel.getId())) {
      return;
    }
    unreadNews++;
    tabContainer.setBadge("news", unreadNews > 99 ? "99+" : Integer.toString(unreadNews));
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

  private void sellAllStocks() {
    try {
      for (Share share : playerController.getPortfolio().getShares()) {
        exchangeController.sell(share, share.quantity(), playerController.getPlayer());
      }
    } catch (RuntimeException ex) {
      errorHandler.accept(ex);
    }
  }

  private void clearNewsBadge() {
    unreadNews = 0;
    tabContainer.setBadge("news", null);
  }

  private boolean saveGame() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Save Game");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Millions save file (*.json)", "*.json"));
    String name = playerController.getName();
    String suggested = (name == null || name.isBlank())
        ? "millions-save.json"
        : "millions-" + name.toLowerCase().replaceAll("\\s+", "-") + ".json";
    chooser.setInitialFileName(suggested);

    File target = chooser.showSaveDialog(view.getScene() == null ? null : view.getScene()
        .getWindow());
    if (target == null) {
      return false;
    }
    if (!target.getName().toLowerCase().endsWith(".json")) {
      target = new File(
          target.getParentFile(),
          target.getName() + ".json"
      );
    }

    try {
      SaveGameUtil.save(target, playerController, exchangeController, gameSettings);
      showInfo("Saved to:\n" + target.getAbsolutePath());
      return true;
    } catch (IOException ex) {
      errorHandler.accept(ex);
      return false;
    }
  }

  public void pushItem(NewsItem item) {
    NewsItem stamped = ensureStamped(item);

    NewsDialog dialog = new NewsDialog(Duration.seconds(10));
    if (stamped.changePercent().compareTo(BigDecimal.ZERO) > 0) {
      dialog.setSentiment(NewsDialog.Sentiment.BULLISH);
    } else {
      dialog.setSentiment(NewsDialog.Sentiment.BEARISH);
    }
    dialog.setSymbol(stamped.sector() == null ? null : stamped.sector().getLabel());
    BigDecimal changePercentFormatted = stamped.changePercent().multiply(BigDecimal.valueOf(100));
    dialog.setChangePercent(changePercentFormatted);
    dialog.setText(stamped.headline());
    dialog.setFooter(stamped.footerText());
    dialog.showIn(newsContainer);
  }

  private static NewsItem ensureStamped(NewsItem item) {
    if (item.when() != null) {
      return item;
    }
    return new NewsItem(
        item.headline(),
        item.sector(), item.volatility(), item.changePercent(),
        item.durationUpdates(), Instant.now(), item.appliedChange()
    );
  }

  private static void showInfo(String message) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle("Millions");
    a.setHeaderText("Game saved");
    a.setContentText(message);
    a.showAndWait();
  }

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
    BigDecimal qty = tx.getShare().quantity();
    BigDecimal price = tx.getShare().pricePerShare();
    BigDecimal fees = BigDecimal.ZERO;
    if (sell) {
      Sale sale = (Sale) tx;
      BigDecimal sp = sale.getSalePricePerShare();
      if (sp != null) {
        price = sp;
      }
      BigDecimal proceeds = sale.getProceeds();
      if (sp != null && proceeds != null) {
        fees = sp.multiply(qty).subtract(proceeds).max(BigDecimal.ZERO);
      }
    }
    return new TradesView.TradeRecord(
        type,
        stock,
        stock.getSymbol(),
        stock.getCompany(),
        qty,
        price,
        when,
        fees
    );
  }

  private void addStatisticsCards() {
    ObservableValue<BigDecimal> netWorth = playerController.getNetWorthProperty();
    ObservableValue<BigDecimal> cash = playerController.getCashProperty();
    ObservableValue<BigDecimal> portfolioValue = playerController.getPlayer()
        .getPortfolio().getTotalMarketValueProperty();
    ObservableValue<BigDecimal> pnl = playerController.getPlayer()
        .getPortfolio().getUnrealizedPnlProperty();

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
        playerStatus(exchangeController, playerController)
    ));
  }

  private static ObservableValue<String> money(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatMoney(source.getValue()), source);
  }

  private static ObservableValue<String> signedMoney(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatSignedMoney(source.getValue()), source);
  }

  private static ObservableValue<String> playerStatus(ExchangeController exchangeController,
                                                      PlayerController playerController) {
    return Bindings.createStringBinding(() -> formatPlayerStatus(playerController
        .getStatus(exchangeController.getWeek().get())), exchangeController.getWeek());
  }

  private static String formatPlayerStatus(PlayerStatus value) {
    String stringValue = value.toString();
    String firstLetter = stringValue.substring(0, 1).toUpperCase();
    String restOfText = value.toString().substring(1).toLowerCase();
    return firstLetter.concat(restOfText);
  }

  private static String formatMoney(BigDecimal v) {
    if (v == null) {
      return "$0.00";
    }
    return "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static String formatSignedMoney(BigDecimal v) {
    if (v == null) {
      return "+$0.00";
    }
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
    if (target == null) {
      return;
    }
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