package ntnu.idatt2003.group15.view;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameView {

    private static final String PLACEHOLDER_ICON =
            "M4 16 L10 10 L14 14 L20 8 M15 8 L20 8 L20 13";
    private static final String DOLLAR_SIGN =
            "M13 3 H11 V5 H9 A3 3 0 0 0 9 11 H13 A2 2 0 0 1 13 15 H8 V13 H6 V17 H11 V19 H13 V17 "
                    + "A3 3 0 0 0 13 11 H9 A1 1 0 0 1 9 9 H15 V7 H13 Z";
    private static final String BULLSEYE =
            "M12 2 A10 10 0 1 0 12 22 A10 10 0 1 0 12 2 Z "
                    + "M12 5 A7 7 0 1 1 12 19 A7 7 0 1 1 12 5 Z "
                    + "M12 8 A4 4 0 1 0 12 16 A4 4 0 1 0 12 8 Z "
                    + "M12 10 A2 2 0 1 1 12 14 A2 2 0 1 1 12 10 Z";
    private static final String TRENDING_UP =
            "M3 17 L9 11 L13 15 L20 8 L20 13 L22 13 L22 4 L13 4 L13 6 L18 6 L13 11 L9 7 L1 15 Z";
    private static final String SEARCH_ICON =
            "M10 4 A6 6 0 1 1 10 16 A6 6 0 1 1 10 4 Z M14.5 14.5 L20 20";

    private final StackPane view = new StackPane();
    private final VBox layout = new VBox();

    private final HeaderView headerView = new HeaderView();
    private final SettingsView settingsView;
    private final StatisticsOverview statisticsOverview = new StatisticsOverview();
    private final TextField searchField = new TextField();

    private final TabContainer tabContainer = new TabContainer(
            new TabContainer.Tab("market",    "Market",    PLACEHOLDER_ICON, new javafx.scene.control.Label("Market")),
            new TabContainer.Tab("portfolio", "Portfolio", PLACEHOLDER_ICON, new javafx.scene.control.Label("Portfolio")),
            new TabContainer.Tab("stats",     "Stats",     PLACEHOLDER_ICON, new javafx.scene.control.Label("Stats")),
            new TabContainer.Tab("trades",    "Trades",    PLACEHOLDER_ICON, new javafx.scene.control.Label("Trades")),
            new TabContainer.Tab("news",      "News",      PLACEHOLDER_ICON, new javafx.scene.control.Label("News"), "1")
    );

    public GameView() {
        this("");
    }

    public GameView(String playerName) {
        settingsView = new SettingsView(view);
        headerView.setPlayerName(playerName);

        HBox header = headerView.createHeader();
        headerView.getSettingsButton().setOnAction(_ -> settingsView.toggle());

        addStatisticsCards();
        HBox searchBar = buildSearchBar();

        VBox.setVgrow(tabContainer.getView(), Priority.ALWAYS);

        settingsView.getView().getStyleClass().add("container");
        statisticsOverview.getView().getStyleClass().add("container");
        tabContainer.getView().getStyleClass().add("container");
        searchBar.getStyleClass().add("container");
        header.getStyleClass().add("container");

        layout.getStyleClass().add("game-layout");
        layout.setFillWidth(false);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getChildren().addAll(
                header,
                settingsView.getView(),
                statisticsOverview.getView(),
                tabContainer.getView(),
                searchBar
        );

        view.getStyleClass().add("game-view");
        view.getChildren().add(layout);
    }

    private void addStatisticsCards() {
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "netWorth", "Net Worth", DOLLAR_SIGN, StatisticsOverview.Tone.BLUE,
                "$10,000.00", "+$0.00 (+0.00%)",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.POSITIVE));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "cash", "Cash Available", DOLLAR_SIGN, StatisticsOverview.Tone.GREEN,
                "$10,000.00", "100.0% of portfolio",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.NEUTRAL));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "portfolio", "Portfolio Value", BULLSEYE, StatisticsOverview.Tone.PURPLE,
                "$0.00", "Invested: $0.00",
                StatisticsOverview.Tone.NEUTRAL, StatisticsOverview.Tone.NEUTRAL));
        statisticsOverview.addCard(new StatisticsOverview.StatCard(
                "pnl", "Unrealized P/L", TRENDING_UP, StatisticsOverview.Tone.GREEN,
                "+$0.00", "+0.00% on holdings",
                StatisticsOverview.Tone.POSITIVE, StatisticsOverview.Tone.POSITIVE));
    }

    private HBox buildSearchBar() {
        Region searchIcon = new Region();
        searchIcon.getStyleClass().add("search-icon");
        searchIcon.setStyle("-fx-shape: \"" + SEARCH_ICON + "\";");

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
}
