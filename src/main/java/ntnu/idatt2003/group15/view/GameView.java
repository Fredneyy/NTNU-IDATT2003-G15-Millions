package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.Objects;
import java.util.Set;

public class GameView {

    private static final Set<String> TABS_WITH_SEARCH = Set.of("market", "portfolio");

    private final StackPane view = new StackPane();

  private final HeaderView headerView;
    private final SettingsView settingsView;
    private final StatisticsOverview statisticsOverview = new StatisticsOverview();
    private final ObservableList<Stock> marketStocks = FXCollections.observableArrayList();
    private final BuyStockDialog buyStockDialog = new BuyStockDialog();
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
    private final VBox statsContent = new VBox(statsView.getView());
    private final VBox tradesContent = new VBox(tradesView.getView());
    private final VBox newsContent = new VBox(newsFeedView.getView());

    private final TabContainer tabContainer;
    public GameView(String playerName, Runnable runnableExit) {
        marketTable = new MarketTableView(marketStocks, stock -> {
          buyStockDialog.show(view, stock);
        }, stock -> {
          stockChartDialog.show(view, stock);
        });
        marketContent = new VBox(marketTable.getView());
        tabContainer = new TabContainer(
            new TabContainer.Tab("market",    "Market",    FontAwesome.LINE_CHART,  marketContent),
            new TabContainer.Tab("portfolio", "Portfolio", FontAwesome.BRIEFCASE,   portfolioContent),
            new TabContainer.Tab("stats",     "Stats",     FontAwesome.BAR_CHART,   statsContent),
            new TabContainer.Tab("trades",    "Trades",    FontAwesome.CLOCK_O,     tradesContent),
            new TabContainer.Tab("news",      "News",      FontAwesome.NEWSPAPER_O, newsContent, "1")
        );
        headerView = new HeaderView(runnableExit);
        settingsView = new SettingsView(view);
        headerView.setPlayerName(playerName);
        view.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/RootStyle.css")).toExternalForm());
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
        seedDemoStocks();
        seedDemoPortfolio();
        seedDemoStats();
        seedDemoTrades();
        seedDemoNews();

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

    private void wireSearchBarToTabs() {
        // Initial state: TabContainer auto-selects the first tab on construction,
        // but our listener attaches after that, so seed the initial placement manually.
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

    private void addStatisticsCards() {
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "netWorth", "Net Worth", FontAwesome.DOLLAR, StatisticsOverview.Tone.BLUE,
                "$10,000.00", "+$0.00 (+0.00%)",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.POSITIVE));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "cash", "Cash Available", FontAwesome.MONEY, StatisticsOverview.Tone.GREEN,
                "$10,000.00", "100.0% of portfolio",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.NEUTRAL));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "portfolio", "Portfolio Value", FontAwesome.BULLSEYE, StatisticsOverview.Tone.PURPLE,
                "$0.00", "Invested: $0.00",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.NEUTRAL));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "pnl", "Unrealized P/L", FontAwesome.LINE_CHART, StatisticsOverview.Tone.GREEN,
                "+$0.00", "+0.00% on holdings",
                StatisticsOverview.Tone.POSITIVE, StatisticsOverview.Tone.POSITIVE));
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

    public StackPane getView() {
        return view;
    }

    public HeaderView getHeaderView() {
        return headerView;
    }

    public SettingsView getSettingsView() {
        return settingsView;
    }

    public StatisticsOverview getStatisticsOverview() {
        return statisticsOverview;
    }

    public TabContainer getTabContainer() {
        return tabContainer;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public MarketTableView getMarketTable() {
        return marketTable;
    }

    public void setMarketStocks(List<Stock> stocks) {
        marketStocks.setAll(stocks);
    }

    /** Placeholder data so the table renders something before a controller wires real stocks. */
    private void seedDemoStocks() {
        Stock aapl = stockWithHistory("AAPL", "Apple Inc.", List.of("Tech"),
                170.10, 172.45, 175.80, 174.20, 177.10, 178.42, 182.16);
        Stock googl = stockWithHistory("GOOGL", "Alphabet Inc.", List.of("Tech"),
                144.20, 142.80, 138.50, 130.20, 122.40, 118.30, 115.65);
        Stock msft = stockWithHistory("MSFT", "Microsoft Corp.", List.of("Tech", "Cloud"),
                420.30, 425.40, 430.60, 435.10, 440.50, 442.10, 444.92);
        Stock nvda = stockWithHistory("NVDA", "NVIDIA Corp.", List.of("Tech", "AI"),
                810.00, 830.20, 845.50, 855.10, 865.10, 880.40, 892.40);
        Stock tsla = stockWithHistory("TSLA", "Tesla Inc.", List.of("Auto", "EV"),
                265.20, 258.10, 252.50, 248.40, 251.30, 244.20, 239.90);
        Stock amzn = stockWithHistory("AMZN", "Amazon.com Inc.", List.of("Tech", "Retail"),
                136.20, 135.50, 134.40, 135.10, 134.99, 135.30, 134.99);
        Stock jpm = stockWithHistory("JPM", "JPMorgan Chase", List.of("Finance"),
                193.40, 195.10, 196.80, 197.50, 198.75, 200.10, 201.10);

        // Demo plug-ins for the new Owned and Status columns. Replace with controller
        // hooks once PortfolioController / event sources are available.
        java.util.Map<String, Integer> owned = java.util.Map.of("AAPL", 12, "NVDA", 3);
        java.util.Map<String, MarketTableView.EventStatus> events = java.util.Map.of(
                "GOOGL", MarketTableView.EventStatus.NEGATIVE,
                "MSFT",  MarketTableView.EventStatus.POSITIVE,
                "AMZN",  MarketTableView.EventStatus.NEGATIVE);
        marketTable.setOwnedLookup(s -> owned.getOrDefault(s.getSymbol(), 0));
        marketTable.setEventLookup(s ->
                events.getOrDefault(s.getSymbol(), MarketTableView.EventStatus.NONE));

        marketStocks.addAll(aapl, googl, msft, nvda, tsla, amzn, jpm);
        marketStocks.addAll(aapl, googl, msft, nvda, tsla, amzn, jpm);
    }

    /** Demo positions for the portfolio tab. Replace with PortfolioController data later. */
    private void seedDemoPortfolio() {
        if (marketStocks.isEmpty()) return;
        portfolioShares.clear();
        // Use realistic avg-buy prices that differ from current prices so the holdings
        // P/L cells in StatsView render colored gains/losses.
        for (Stock s : marketStocks) {
            switch (s.getSymbol()) {
                case "AMZN"  -> portfolioShares.add(new Share(s, new BigDecimal("12"), new BigDecimal("153.92")));
                case "GOOGL" -> portfolioShares.add(new Share(s, new BigDecimal("1"),  new BigDecimal("141.33")));
                case "NVDA"  -> portfolioShares.add(new Share(s, new BigDecimal("1"),  new BigDecimal("492.77")));
                default -> { /* not owned */ }
            }
        }
        portfolioTable.setEventLookup(stock -> {
            switch (stock.getSymbol()) {
                case "AMZN": return MarketTableView.EventStatus.POSITIVE;
                default:     return MarketTableView.EventStatus.NONE;
            }
        });
        // Mirror the same positions in the Stats tab's holdings table
        statsView.setHoldings(portfolioShares);
    }

    public PortfolioTableView getPortfolioTable() {
        return portfolioTable;
    }

    public StatsView getStatsView() {
        return statsView;
    }

    public TradesView getTradesView() {
        return tradesView;
    }

    public NewsFeedView getNewsFeedView() {
        return newsFeedView;
    }

    /** Demo market events to show off the News tab. Wire to PriceEvent source later. */
    private void seedDemoNews() {
        java.time.Instant now = java.time.Instant.now();
        newsFeedView.setEvents(List.of(
                new NewsFeedView.NewsRecord(
                        NewsFeedView.Sentiment.BEARISH,
                        "AMZN",
                        new BigDecimal("-8.0"),
                        "Amazon.com Inc.: Earnings Miss Expectations",
                        "Quarterly results fall short of analyst predictions, citing market headwinds.",
                        new BigDecimal("1.6"), 22, "Earnings Miss", now),
                new NewsFeedView.NewsRecord(
                        NewsFeedView.Sentiment.BULLISH,
                        "SOFI",
                        new BigDecimal("25.0"),
                        "SoFi Technologies: Revolutionary Product Announced",
                        "Company unveils groundbreaking technology that could transform the industry.",
                        new BigDecimal("1.8"), 19, "Breakthrough", now),
                new NewsFeedView.NewsRecord(
                        NewsFeedView.Sentiment.BULLISH,
                        "NFLX",
                        new BigDecimal("12.0"),
                        "Netflix Inc.: Strategic Partnership Formed",
                        "New alliance opens fresh revenue streams in emerging markets.",
                        new BigDecimal("1.2"), 14, "Partnership", now)
        ));
        tabContainer.setBadge("news", "3");
    }

    /** Demo transactions to show off the Trades tab. Wire to TransactionArchive later. */
    private void seedDemoTrades() {
        java.time.Instant now = java.time.Instant.now();
        java.time.Instant sevenMinAgo = now.minus(java.time.Duration.ofMinutes(7));
        tradesView.setTrades(List.of(
                new TradesView.TradeRecord(TradesView.TradeType.BUY,
                        "NVDA", "NVIDIA Corporation",
                        new BigDecimal("1"), new BigDecimal("492.77"), sevenMinAgo),
                new TradesView.TradeRecord(TradesView.TradeType.BUY,
                        "GOOGL", "Alphabet Inc.",
                        new BigDecimal("1"), new BigDecimal("141.33"), sevenMinAgo),
                new TradesView.TradeRecord(TradesView.TradeType.BUY,
                        "AMZN", "Amazon.com Inc.",
                        new BigDecimal("12"), new BigDecimal("153.92"), sevenMinAgo)
        ));
        tabContainer.setBadge("trades", "3");
    }

    /** Demo stats numbers — wire to a controller later. Mirrors the reference screenshot. */
    private void seedDemoStats() {
        statsView.setTotalTrades(3, 3, 0);
        statsView.setRealizedPL("+$0.00", StatsView.Tone.POSITIVE);
        statsView.setUnrealizedPL("+$474.86", StatsView.Tone.PURPLE);
        statsView.setWinRate("0.0%", 0, 0);
        statsView.setTotalReturn("+$474.86", "(+4.75%)", StatsView.Tone.POSITIVE);
        statsView.setAvgTradeSize("$827.05");
        statsView.setMostTraded("NVDA");
    }

    public void setPortfolioShares(List<Share> shares) {
        portfolioShares.setAll(shares);
    }

    private static Stock stockWithHistory(String symbol, String company,
                                          List<String> categories, double... prices) {
        Stock s = new Stock(symbol, company, BigDecimal.valueOf(prices[0]));
        for (int i = 1; i < prices.length; i++) {
            s.addNewSalesPrice(BigDecimal.valueOf(prices[i]));
        }
        s.setCategories(categories);
        return s;
    }
}
