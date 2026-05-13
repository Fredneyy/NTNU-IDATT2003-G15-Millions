package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.controller.ExchangeController;
import ntnu.idatt2003.group15.controller.PlayerController;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

public class GameView {

  private static final Set<String> TABS_WITH_SEARCH = Set.of("market", "portfolio");

  private final StackPane view = new StackPane();

  private final HeaderView headerView;
  private final SettingsView settingsView;
  private final StatisticsOverview statisticsOverview = new StatisticsOverview();
  private final ObservableList<Stock> marketStocks = FXCollections.observableArrayList();
  private final BuyStockDialog buyStockDialog;
  private final StockChartDialog stockChartDialog = new StockChartDialog();
  private final MarketTableView marketTable;
  private final ObservableList<Share> portfolioShares = FXCollections.observableArrayList();
  private final PortfolioTableView portfolioTable = new PortfolioTableView(portfolioShares);
  private final StatsView statsView = new StatsView();
  private final TradesView tradesView = new TradesView();
  private final NewsFeedView newsFeedView = new NewsFeedView();
  private final TextField searchField = new TextField();
  private final HBox searchBar;

  private final VBox marketContent;
  private final VBox portfolioContent = new VBox(portfolioTable.getView());
  private final PlayerController playerController;
  private final ExchangeController exchangeController;

  private final TabContainer tabContainer;

  public GameView(PlayerController player, ExchangeController exchange, Runnable runnableExit) {
    this.playerController = Objects.requireNonNull(player);
    this.exchangeController = Objects.requireNonNull(exchange);

    buyStockDialog = new BuyStockDialog(
        exchangeController.cashProperty(),
        exchangeController::buy);

    marketTable = new MarketTableView(marketStocks,
        stock -> buyStockDialog.show(view, stock),
        stock -> stockChartDialog.show(view, stock));
    marketContent = new VBox(marketTable.getView());
    VBox statsContent = new VBox(statsView.getView());
    VBox tradesContent = new VBox(tradesView.getView());
    VBox newsContent = new VBox(newsFeedView.getView());
    tabContainer = new TabContainer(
        new TabContainer.Tab("market",    "Market",    FontAwesome.LINE_CHART,  marketContent),
        new TabContainer.Tab("portfolio", "Portfolio", FontAwesome.BRIEFCASE,   portfolioContent),
        new TabContainer.Tab("stats",     "Stats",     FontAwesome.BAR_CHART,   statsContent),
        new TabContainer.Tab("trades",    "Trades",    FontAwesome.CLOCK_O,     tradesContent),
        new TabContainer.Tab("news",      "News",      FontAwesome.NEWSPAPER_O, newsContent, "1")
    );
    headerView = new HeaderView(runnableExit);
    settingsView = new SettingsView(view);
    headerView.setPlayerName(player.getName());
    view.getStylesheets().add(Objects.requireNonNull(
        getClass().getResource("/style/RootStyle.css")).toExternalForm());
    HBox header = headerView.createHeader();
    headerView.getSettingsButton().setOnAction(_ -> settingsView.toggle());

    addStatisticsCards();
    searchBar = buildSearchBar();
    styleTabContent(marketContent, portfolioContent, statsContent, tradesContent, newsContent);
    VBox.setVgrow(marketTable.getView(), Priority.ALWAYS);
    VBox.setVgrow(portfolioTable.getView(), Priority.ALWAYS);
    searchField.textProperty().addListener((_, _, q) -> {
      marketTable.setSearchFilter(q);
      portfolioTable.setSearchFilter(q);
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
    layout.setAlignment(Pos.TOP_CENTER);
    layout.getChildren().addAll(
        header,
        settingsView.getView(),
        statisticsOverview.getView(),
        tabContainer.getView()
    );

    view.getStyleClass().add("game-view");
    view.getChildren().add(layout);
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
  }

  private static ObservableValue<String> money(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatMoney(source.getValue()), source);
  }

  private static ObservableValue<String> signedMoney(ObservableValue<BigDecimal> source) {
    return Bindings.createStringBinding(() -> formatSignedMoney(source.getValue()), source);
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

  public StackPane getView() { return view; }
  public HeaderView getHeaderView() { return headerView; }
  public SettingsView getSettingsView() { return settingsView; }
  public StatisticsOverview getStatisticsOverview() { return statisticsOverview; }
  public TabContainer getTabContainer() { return tabContainer; }
  public TextField getSearchField() { return searchField; }
  public MarketTableView getMarketTable() { return marketTable; }
  public PortfolioTableView getPortfolioTable() { return portfolioTable; }
  public StatsView getStatsView() { return statsView; }
  public TradesView getTradesView() { return tradesView; }
  public NewsFeedView getNewsFeedView() { return newsFeedView; }

  public void setMarketStocks(List<Stock> stocks) { marketStocks.setAll(stocks); }
  public void setPortfolioShares(List<Share> shares) { portfolioShares.setAll(shares); }
}