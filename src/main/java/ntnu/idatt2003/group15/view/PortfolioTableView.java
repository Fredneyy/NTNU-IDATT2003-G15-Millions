package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Card-styled portfolio table. Each row is one {@link Share} holding. Header
 * shows the position count and aggregate market value, both bound live to the
 * {@link PortfolioController}. Action column has a Sell button instead of Buy.
 */
public class PortfolioTableView {

    private final VBox view = new VBox();
    private final Label title = new Label("Your Portfolio");
    private final Label countLabel = new Label("0 positions");
    private final Label totalValueLabel = new Label("$0.00");
    private final TableView<Share> table = new TableView<>();
    private final PortfolioController portfolioController;
    private final FilteredList<Share> filtered;
    private final Consumer<Share> onSellPressed;
    private final Consumer<Stock> onChartPressed;

    public PortfolioTableView(PortfolioController portfolioController,
                              Consumer<Share> onSellPressed,
                              Consumer<Stock> onChartPressed) {
        this.portfolioController = Objects.requireNonNull(portfolioController);
        this.onSellPressed = onSellPressed;
        this.onChartPressed = onChartPressed;
        this.filtered = new FilteredList<>(portfolioController.getListProperty(), _ -> true);

        buildTable();
        bindHeaderSummary();

        VBox.setVgrow(table, Priority.ALWAYS);
        view.getStyleClass().addAll("market-table", "portfolio-table");
        view.getChildren().addAll(buildHeader(), table);
    }

    private HBox buildHeader() {
        title.getStyleClass().add("portfolio-table-title");
        countLabel.getStyleClass().add("portfolio-table-subtitle");

        VBox titleBox = new VBox(title, countLabel);
        titleBox.getStyleClass().add("portfolio-table-title-box");

        Label totalCaption = new Label("Total Value");
        totalCaption.getStyleClass().add("portfolio-total-caption");
        totalValueLabel.getStyleClass().add("portfolio-total-value");
        VBox totalBox = new VBox(totalCaption, totalValueLabel);
        totalBox.getStyleClass().add("portfolio-total-box");
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(titleBox, spacer, totalBox);
        row.getStyleClass().add("portfolio-table-header");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void bindHeaderSummary() {
        ObservableList<Share> shares = portfolioController.getListProperty();
        countLabel.textProperty().bind(Bindings.createStringBinding(
                () -> shares.size() + (shares.size() == 1 ? " position" : " positions"),
                shares));

        ObservableValue<BigDecimal> totalValue = portfolioController.totalMarketValueProperty();
        totalValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> formatMoney(totalValue.getValue()), totalValue));
    }

    private void buildTable() {
        TableColumn<Share, Share> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        symbolCol.setCellFactory(_ -> symbolCell());
        styleCellsAs(symbolCol, "col-symbol");

        TableColumn<Share, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(c -> c.getValue().stock().companyProperty());
        styleCellsAs(companyCol, "col-company");

        TableColumn<Share, BigDecimal> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(c -> c.getValue().quantityProperty());
        qtyCol.setCellFactory(_ -> quantityCell());
        styleCellsAs(qtyCol, "col-quantity");

        TableColumn<Share, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> c.getValue().stock().getPriceBinding());
        priceCol.setCellFactory(_ -> moneyCell());
        styleCellsAs(priceCol, "col-price");

        TableColumn<Share, BigDecimal> totalCol = new TableColumn<>("Total Value");
        totalCol.setCellValueFactory(c -> {
            Share s = c.getValue();
            return Bindings.createObjectBinding(
                    () -> s.stock().getSalesPrice().multiply(s.quantity()),
                    s.stock().getPriceBinding());
        });
        totalCol.setCellFactory(_ -> moneyCell());
        styleCellsAs(totalCol, "col-total");

        TableColumn<Share, Share> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(c -> {
            Share s = c.getValue();
            return Bindings.createObjectBinding(() -> s, s.stock().getPriceBinding());
        });
        changeCol.setCellFactory(_ -> combinedChangeCell());
        styleCellsAs(changeCol, "col-change");

        TableColumn<Share, Share> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        actionCol.setCellFactory(_ -> sellButtonCell(onSellPressed, onChartPressed));
        actionCol.setSortable(false);
        styleCellsAs(actionCol, "col-action");

        table.getColumns().add(symbolCol);
        table.getColumns().add(companyCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(totalCol);
        table.getColumns().add(changeCol);
        table.getColumns().add(actionCol);
        
        table.setItems(filtered);
        table.setPlaceholder(EmptyState.create(
                FontAwesome.PIE_CHART,
                "No holdings yet",
                "Your portfolio will appear here.\n"
                        + "Buy a stock from the Market tab to start your collection."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().addAll("market-table-inner", "portfolio-table-inner");
    }

    private static <S, T> void styleCellsAs(TableColumn<S, T> column, String styleClass) {
        column.getStyleClass().add(styleClass);
    }

    private static String formatMoney(BigDecimal v) {
        if (v == null) return "$0.00";
        return "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static TableCell<Share, Share> symbolCell() {
        return new TableCell<>() {
            // mirrors MarketTableView: avatar-only column, no separate symbol label
            private final Label avatarLabel = new Label();
            private final StackPane avatar = new StackPane(avatarLabel);
            private final HBox wrapper = new HBox(avatar);
            {
                avatar.getStyleClass().add("symbol-avatar");
                avatarLabel.getStyleClass().add("symbol-avatar-text");
                wrapper.getStyleClass().add("symbol-cell");
                wrapper.setSpacing(12);
                avatarLabel.setWrapText(true);
            }

            @Override
            protected void updateItem(Share share, boolean empty) {
                super.updateItem(share, empty);
                if (empty || share == null) {
                    setGraphic(null);
                    return;
                }
                String sym = share.stock().getSymbol();
                avatarLabel.setText(sym.length() >= 3 ? sym.substring(0, 3) : sym);
                setGraphic(wrapper);
            }
        };
    }

    private static TableCell<Share, BigDecimal> quantityCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value.stripTrailingZeros().toPlainString());
                }
            }
        };
    }

    private static TableCell<Share, BigDecimal> moneyCell() {
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

    private static TableCell<Share, Share> combinedChangeCell() {
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
            protected void updateItem(Share share, boolean empty) {
                super.updateItem(share, empty);
                wrapper.getStyleClass().removeAll("change-cell--positive", "change-cell--negative");
                if (empty || share == null) {
                    setGraphic(null);
                    return;
                }
                Stock stock = share.stock();
                BigDecimal delta = stock.getLatestPriceChange();
                BigDecimal pct = stock.getLatestPriceChangeRelative().movePointRight(2);
                int sign = delta.signum();
                wrapper.getStyleClass().add(
                        sign >= 0 ? "change-cell--positive" : "change-cell--negative");

                arrow.setIconCode(sign >= 0 ? FontAwesome.LINE_CHART : FontAwesome.AREA_CHART);
                amount.setText((sign >= 0 ? "+" : "-") + "$"
                        + delta.abs().setScale(2, RoundingMode.HALF_UP).toPlainString());
                percent.setText("(" + (sign >= 0 ? "+" : "")
                        + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%)");
                setGraphic(wrapper);
            }
        };
    }

    private static TableCell<Share, Share> sellButtonCell(Consumer<Share> onSell,
                                                          Consumer<Stock> onChart) {
        return new TableCell<>() {
            private final Button graphButton = new Button();
            private final Button sellButton = new Button("Sell");
            private final HBox wrapper = new HBox(graphButton, sellButton);
            {
                graphButton.setGraphic(new FontIcon(FontAwesome.AREA_CHART));
                graphButton.getStyleClass().add("market-graph-button");
                graphButton.setOnAction(_ -> {
                    Share s = getItem();
                    if (s != null && onChart != null) onChart.accept(s.stock());
                });

                sellButton.getStyleClass().add("market-sell-button");
                sellButton.setOnAction(_ -> {
                    Share s = getItem();
                    if (s != null && onSell != null) onSell.accept(s);
                });

                wrapper.getStyleClass().add("market-action-cell");
                wrapper.setSpacing(8);
            }

            @Override
            protected void updateItem(Share share, boolean empty) {
                super.updateItem(share, empty);
                setGraphic(empty || share == null ? null : wrapper);
            }
        };
    }


    public VBox getView() { return view; }

    public ObservableList<Share> getShares() { return portfolioController.getListProperty(); }

    /** Filter rows by symbol or company substring (case insensitive). Empty resets. */
    public void setSearchFilter(String query) {
        if (query == null || query.isBlank()) {
            filtered.setPredicate(_ -> true);
            return;
        }
        String q = query.trim().toLowerCase();
        filtered.setPredicate(s ->
                s.stock().getSymbol().toLowerCase().contains(q)
                        || s.stock().getCompany().toLowerCase().contains(q));
    }
}
