package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.scene.shape.Polyline;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Card-styled live market table. Renders one row per {@link Stock} and
 * recolours change columns based on direction. Backing list is observable so
 * controller updates propagate automatically.
 */
public class MarketTableView {

    private final VBox view = new VBox();
    private final Label title = new Label("Live Market");
    private final Label subtitle = new Label("Real-time stock prices");
    private final TableView<Stock> table = new TableView<>();
    private final FilteredList<Stock> filteredStocks;
    private final ObservableList<Stock> stocks;

    public MarketTableView() {
        this(FXCollections.observableArrayList());
    }

    public MarketTableView(ObservableList<Stock> stocks) {
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
        TableColumn<Stock, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(c -> c.getValue().symbolProperty());
        styleCellsAs(symbolCol, "col-symbol");

        TableColumn<Stock, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(c -> c.getValue().companyProperty());
        styleCellsAs(companyCol, "col-company");

        TableColumn<Stock, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getSalesPrice()));
        priceCol.setCellFactory(_ -> moneyCell());
        styleCellsAs(priceCol, "col-price");

        TableColumn<Stock, BigDecimal> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getLatestPriceChange()));
        changeCol.setCellFactory(_ -> signedMoneyCell());
        styleCellsAs(changeCol, "col-change");

        TableColumn<Stock, BigDecimal> changePctCol = new TableColumn<>("Change %");
        changePctCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getLatestPriceChangeRelative()));
        changePctCol.setCellFactory(_ -> percentCell());
        styleCellsAs(changePctCol, "col-change-pct");

        TableColumn<Stock, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.join(", ", c.getValue().getCategories())));
        styleCellsAs(categoryCol, "col-category");

        TableColumn<Stock, Stock> graphCol = new TableColumn<>("Graph");
        graphCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        graphCol.setCellFactory(_ -> sparklineCell());
        graphCol.setSortable(false);
        styleCellsAs(graphCol, "col-graph");

        TableColumn<Stock, Stock> actionCol = new TableColumn<>("");
        actionCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        actionCol.setCellFactory(_ -> buyButtonCell());
        actionCol.setSortable(false);
        styleCellsAs(actionCol, "col-action");

        table.getColumns().setAll(
                symbolCol, companyCol, priceCol, changeCol, changePctCol,
                categoryCol, graphCol, actionCol);

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

    private static TableCell<Stock, BigDecimal> signedMoneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                getStyleClass().removeAll("cell-positive", "cell-negative");
                if (empty || value == null) {
                    setText(null);
                    return;
                }
                int sign = value.signum();
                BigDecimal abs = value.abs().setScale(2, RoundingMode.HALF_UP);
                String prefix = sign >= 0 ? "+$" : "-$";
                setText(prefix + abs.toPlainString());
                getStyleClass().add(sign >= 0 ? "cell-positive" : "cell-negative");
            }
        };
    }

    private static TableCell<Stock, BigDecimal> percentCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                getStyleClass().removeAll("cell-positive", "cell-negative");
                if (empty || value == null) {
                    setText(null);
                    return;
                }
                BigDecimal pct = value.movePointRight(2).setScale(2, RoundingMode.HALF_UP);
                int sign = pct.signum();
                String prefix = sign >= 0 ? "+" : "";
                setText(prefix + pct.toPlainString() + "%");
                getStyleClass().add(sign >= 0 ? "cell-positive" : "cell-negative");
            }
        };
    }

    private static TableCell<Stock, Stock> buyButtonCell() {
        return new TableCell<>() {
            private final Button graphButton = new Button();
            private final Button buyButton = new Button("Buy");
            private final HBox wrapper = new HBox(graphButton, buyButton);
            {
                graphButton.setGraphic(new FontIcon(FontAwesome.AREA_CHART));
                graphButton.getStyleClass().add("market-graph-button");

                buyButton.setGraphic(new FontIcon(FontAwesome.SHOPPING_CART));
                buyButton.getStyleClass().add("market-buy-button");

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

    private static TableCell<Stock, Stock> sparklineCell() {
        return new TableCell<>() {
            private final Polyline line = new Polyline();
            private final StackPane wrapper = new StackPane(line);
            {
                line.getStyleClass().add("sparkline");
                wrapper.getStyleClass().add("sparkline-wrapper");
                wrapper.setMinSize(80, 28);
                wrapper.setPrefSize(80, 28);
                wrapper.setMaxSize(80, 28);
            }

            @Override
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setGraphic(null);
                    return;
                }
                renderLine(stock.getHistoricalPrices());
                line.getStyleClass().removeAll("sparkline-positive", "sparkline-negative");
                line.getStyleClass().add(
                        stock.getLatestPriceChange().signum() >= 0
                                ? "sparkline-positive" : "sparkline-negative");
                setGraphic(wrapper);
            }

            private void renderLine(List<BigDecimal> prices) {
                line.getPoints().clear();
                if (prices == null || prices.size() < 2) return;

                double w = 80, h = 24, padX = 2, padY = 2;
                double minP = prices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0);
                double maxP = prices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);
                double range = Math.max(maxP - minP, 1e-9);
                int n = prices.size();

                for (int i = 0; i < n; i++) {
                    double x = padX + (w - 2 * padX) * i / (n - 1);
                    double normY = (prices.get(i).doubleValue() - minP) / range;
                    double y = h - padY - (h - 2 * padY) * normY; // flip Y so higher = up
                    line.getPoints().addAll(x, y);
                }
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
}
