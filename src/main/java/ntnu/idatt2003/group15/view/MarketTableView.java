package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Card-styled live market table. Renders one row per {@link Stock} and
 * recolours change columns based on direction. Backing list is observable so
 * controller updates propagate automatically.
 */
public class MarketTableView {

    /** Status of an upcoming/active price event on a stock. */
    public enum EventStatus { NONE, POSITIVE, NEGATIVE }

    private final VBox view = new VBox();
    private final Label title = new Label("Live Market");
    private final Label subtitle = new Label("Real-time stock prices");
    private final TableView<Stock> table = new TableView<>();
    private final FilteredList<Stock> filteredStocks;
    private final ObservableList<Stock> stocks;

    /** Pluggable lookups: a controller can supply real portfolio/event sources later. */
    private Function<Stock, Integer> ownedLookup = _ -> 0;
    private Function<Stock, EventStatus> eventLookup = _ -> EventStatus.NONE;
    private final Consumer<Stock> onBuyPressed;
    private final Consumer<Stock> onChartPressed;

    public MarketTableView(ObservableList<Stock> stocks, Consumer<Stock> onBuyPressed,  Consumer<Stock> onChartPressed) {
        this.onBuyPressed = onBuyPressed;
        this.onChartPressed = onChartPressed;
        this.stocks = stocks;
        this.filteredStocks = new FilteredList<>(stocks, _ -> true);

        buildHeader();
        buildTable();

        VBox.setVgrow(table, Priority.ALWAYS);
        view.getStyleClass().add("market-table");
        view.getChildren().addAll(buildHeaderRow(), table);
    }

    private void buildHeader() {
        title.getStyleClass().add("market-table-title");
        subtitle.getStyleClass().add("market-table-subtitle");
    }

    private HBox buildHeaderRow() {
        FontIcon icon = new FontIcon(FontAwesome.LINE_CHART);
        icon.getStyleClass().add("market-table-icon");

        VBox titleBox = new VBox(title, subtitle);
        titleBox.getStyleClass().add("market-table-title-box");

        HBox row = new HBox(icon, titleBox);
        row.getStyleClass().add("market-table-header");
        return row;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Stock, Stock> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        symbolCol.setCellFactory(_ -> symbolCell());
        styleCellsAs(symbolCol, "col-symbol");

        TableColumn<Stock, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(c -> c.getValue().companyProperty());
        styleCellsAs(companyCol, "col-company");

        TableColumn<Stock, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getSalesPrice()));
        priceCol.setCellFactory(_ -> moneyCell());
        styleCellsAs(priceCol, "col-price");

        TableColumn<Stock, Stock> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        changeCol.setCellFactory(_ -> combinedChangeCell());
        styleCellsAs(changeCol, "col-change");

        TableColumn<Stock, Stock> volatilityCol = new TableColumn<>("Volatility");
        volatilityCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        volatilityCol.setCellFactory(_ -> volatilityCell());
        styleCellsAs(volatilityCol, "col-volatility");

        TableColumn<Stock, Stock> ownedCol = new TableColumn<>("Owned");
        ownedCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        ownedCol.setCellFactory(_ -> ownedCell(this::lookupOwned));
        styleCellsAs(ownedCol, "col-owned");

        TableColumn<Stock, Stock> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        statusCol.setCellFactory(_ -> statusCell(this::lookupEvent));
        styleCellsAs(statusCol, "col-status");

        TableColumn<Stock, Stock> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        actionCol.setCellFactory(_ -> buyButtonCell());
        actionCol.setSortable(false);
        styleCellsAs(actionCol, "col-action");

        table.getColumns().setAll(
                symbolCol, companyCol, priceCol, changeCol,
                volatilityCol, ownedCol, statusCol, actionCol);

        table.setItems(filteredStocks);
        table.setPlaceholder(new Label("No stocks match your filter."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("market-table-inner");
    }

    private static <S, T> void styleCellsAs(TableColumn<S, T> column, String styleClass) {
        column.getStyleClass().add(styleClass);
    }

    private static TableCell<Stock, BigDecimal> moneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("$" + value.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }
        };
    }

    /** Symbol cell: gradient avatar with the first two letters + symbol text. */
    private static TableCell<Stock, Stock> symbolCell() {
        return new TableCell<>() {
            private final Label avatarLabel = new Label();
            private final StackPane avatar = new StackPane(avatarLabel);
            private final Label symbolLabel = new Label();
            private final HBox wrapper = new HBox(avatar, symbolLabel);
            {
                avatar.getStyleClass().add("symbol-avatar");
                avatarLabel.getStyleClass().add("symbol-avatar-text");
                symbolLabel.getStyleClass().add("symbol-text");
                wrapper.getStyleClass().add("symbol-cell");
                wrapper.setSpacing(12);
            }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setGraphic(null);
                    return;
                }
                String sym = stock.getSymbol();
                avatarLabel.setText(sym.length() >= 2 ? sym.substring(0, 2) : sym);
                symbolLabel.setText(sym);
                setGraphic(wrapper);
            }
        };
    }

    /** Combined change cell: trend arrow + signed $ amount + (% in parentheses). */
    private static TableCell<Stock, Stock> combinedChangeCell() {
        return new TableCell<>() {
            private final FontIcon arrow = new FontIcon();
            private final Label amount = new Label();
            private final Label percent = new Label();
            private final HBox wrapper = new HBox(arrow, amount, percent);
            {
                wrapper.getStyleClass().add("change-cell");
                amount.getStyleClass().add("change-amount");
                percent.getStyleClass().add("change-percent");
                wrapper.setSpacing(6);
            }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                wrapper.getStyleClass().removeAll("change-cell--positive", "change-cell--negative");
                if (empty || stock == null) {
                    setGraphic(null);
                    return;
                }
                BigDecimal delta = stock.getLatestPriceChange();
                BigDecimal pct = stock.getLatestPriceChangeRelative().movePointRight(2);
                int sign = delta.signum();
                String tone = sign >= 0 ? "change-cell--positive" : "change-cell--negative";
                wrapper.getStyleClass().add(tone);

                arrow.setIconCode(sign >= 0 ? FontAwesome.LINE_CHART : FontAwesome.AREA_CHART);
                amount.setText((sign >= 0 ? "+" : "-") + "$"
                        + delta.abs().setScale(2, RoundingMode.HALF_UP).toPlainString());
                percent.setText("(" + (sign >= 0 ? "+" : "")
                        + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%)");
                setGraphic(wrapper);
            }
        };
    }

    /** Volatility cell: derives LOW/MED/HIGH from price history. */
    private static TableCell<Stock, Stock> volatilityCell() {
        return new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("volatility-badge"); }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                badge.getStyleClass().removeAll(
                        "volatility-badge--low",
                        "volatility-badge--med",
                        "volatility-badge--high");
                if (empty || stock == null) {
                    setGraphic(null);
                    return;
                }
                double v = computeVolatility(stock.getHistoricalPrices());
                String label;
                String toneClass;
                if (v < 0.02) {
                    label = "LOW";
                    toneClass = "volatility-badge--low";
                } else if (v < 0.06) {
                    label = "MED";
                    toneClass = "volatility-badge--med";
                } else {
                    label = "HIGH";
                    toneClass = "volatility-badge--high";
                }
                badge.setText(label);
                badge.getStyleClass().add(toneClass);
                setGraphic(badge);
            }

            private double computeVolatility(List<BigDecimal> prices) {
                if (prices == null || prices.size() < 2) return 0;
                double mean = prices.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
                if (mean == 0) return 0;
                double variance = prices.stream()
                        .mapToDouble(p -> Math.pow(p.doubleValue() - mean, 2))
                        .average().orElse(0);
                return Math.sqrt(variance) / mean; // coefficient of variation
            }
        };
    }

    /** Owned cell: shows shares owned or em-dash when zero. */
    private static TableCell<Stock, Stock> ownedCell(Function<Stock, Integer> lookup) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                getStyleClass().removeAll("cell-muted", "cell-owned");
                if (empty || stock == null) {
                    setText(null);
                    return;
                }
                Integer count = lookup.apply(stock);
                if (count == null || count <= 0) {
                    setText("—");
                    getStyleClass().add("cell-muted");
                } else {
                    setText(String.valueOf(count));
                    getStyleClass().add("cell-owned");
                }
            }
        };
    }

    /** Status cell: warning + EVENT text when stock has an active event. */
    private static TableCell<Stock, Stock> statusCell(Function<Stock, EventStatus> lookup) {
        return new TableCell<>() {
            private final FontIcon warning = new FontIcon(FontAwesome.EXCLAMATION_TRIANGLE);
            private final Label text = new Label("EVENT");
            private final HBox badge = new HBox(warning, text);
            {
                badge.getStyleClass().add("status-badge");
                badge.setSpacing(6);
                text.getStyleClass().add("status-badge-text");
            }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                badge.getStyleClass().removeAll("status-badge--positive", "status-badge--negative");
                getStyleClass().removeAll("cell-muted");
                if (empty || stock == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                EventStatus status = lookup.apply(stock);
                if (status == null || status == EventStatus.NONE) {
                    setText("—");
                    setGraphic(null);
                    getStyleClass().add("cell-muted");
                    return;
                }
                badge.getStyleClass().add(
                        status == EventStatus.POSITIVE
                                ? "status-badge--positive" : "status-badge--negative");
                setText(null);
                setGraphic(badge);
            }
        };
    }

    private Integer lookupOwned(Stock s) { return ownedLookup.apply(s); }
    private EventStatus lookupEvent(Stock s) { return eventLookup.apply(s); }

    private TableCell<Stock, Stock> buyButtonCell() {
        return new TableCell<>() {
            private final Button graphButton = new Button();
            private final Button buyButton = new Button("Buy");
            private final HBox wrapper = new HBox(graphButton, buyButton);
            {
                graphButton.setGraphic(new FontIcon(FontAwesome.AREA_CHART));
                graphButton.getStyleClass().add("market-graph-button");
                graphButton.setOnAction(_ -> {
                    Stock stock = getItem();
                    if (stock != null && onChartPressed != null) {
                        onChartPressed.accept(stock);
                    }
                });

                buyButton.setGraphic(new FontIcon(FontAwesome.SHOPPING_CART));
                buyButton.getStyleClass().add("market-buy-button");
                buyButton.setOnAction(_ -> {
                    Stock stock = getItem();
                    if (stock != null && onBuyPressed != null) {
                        onBuyPressed.accept(stock);
                    }
                });

                wrapper.getStyleClass().add("market-action-cell");
                wrapper.setSpacing(8);
            }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                setGraphic(empty || stock == null ? null : wrapper);
            }
        };
    }

    /** Filter table rows by symbol/company substring (case insensitive). Empty resets. */
    public void setSearchFilter(String query) {
        if (query == null || query.isBlank()) {
            filteredStocks.setPredicate(_ -> true);
            return;
        }
        String q = query.trim().toLowerCase();
        filteredStocks.setPredicate(s ->
                s.getSymbol().toLowerCase().contains(q)
                        || s.getCompany().toLowerCase().contains(q));
    }

    public VBox getView() { return view; }
    public TableView<Stock> getTable() { return table; }
    public ObservableList<Stock> getStocks() { return stocks; }

    /** Plug in the source of "shares owned" per stock (typically from PortfolioController). */
    public void setOwnedLookup(Function<Stock, Integer> lookup) {
        this.ownedLookup = lookup == null ? _ -> 0 : lookup;
        table.refresh();
    }

    /** Plug in the source of event status per stock. */
    public void setEventLookup(Function<Stock, EventStatus> lookup) {
        this.eventLookup = lookup == null ? _ -> EventStatus.NONE : lookup;
        table.refresh();
    }
}
