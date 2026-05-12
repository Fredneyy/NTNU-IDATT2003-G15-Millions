package ntnu.idatt2003.group15.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private final TextField searchField = new TextField();
    private final HBox searchBar;

    private final VBox marketContent = new VBox(new Label("Market"));
    private final VBox portfolioContent = new VBox(new Label("Portfolio"));
    private final VBox statsContent = new VBox(new Label("Stats"));
    private final VBox tradesContent = new VBox(new Label("Trades"));
    private final VBox newsContent = new VBox(new Label("News"));

    private final TabContainer tabContainer = new TabContainer(
            new TabContainer.Tab("market",    "Market",    FontAwesome.LINE_CHART,  marketContent),
            new TabContainer.Tab("portfolio", "Portfolio", FontAwesome.BRIEFCASE,   portfolioContent),
            new TabContainer.Tab("stats",     "Stats",     FontAwesome.BAR_CHART,   statsContent),
            new TabContainer.Tab("trades",    "Trades",    FontAwesome.CLOCK_O,     tradesContent),
            new TabContainer.Tab("news",      "News",      FontAwesome.NEWSPAPER_O, newsContent, "1")
    );

    public GameView(String playerName, Runnable runnableExit) {
        headerView = new HeaderView(runnableExit);
        settingsView = new SettingsView(view);
        headerView.setPlayerName(playerName);
        view.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/RootStyle.css")).toExternalForm());
        HBox header = headerView.createHeader();
        headerView.getSettingsButton().setOnAction(_ -> settingsView.toggle());

        addStatisticsCards();
        searchBar = buildSearchBar();
        styleTabContent(marketContent, portfolioContent, statsContent, tradesContent, newsContent);

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
}
