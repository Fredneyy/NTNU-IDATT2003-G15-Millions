package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.Share;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Stats tab content. Four KPI cards on top + a Performance Summary card below.
 *
 * Values are exposed via setters so a controller can wire real data.
 */
public class StatsView {

    /** Color tone for value text. */
    public enum Tone { NEUTRAL, POSITIVE, NEGATIVE, BLUE, GREEN, PURPLE, GOLD }

    private final VBox view = new VBox();

    // Top-row cards (live references so setters can update them)
    private final KpiCard totalTrades = new KpiCard("Total Trades",
            FontAwesome.BAR_CHART, Tone.BLUE);
    private final KpiCard realizedPL = new KpiCard("Realized P/L",
            FontAwesome.DOLLAR, Tone.GREEN);
    private final KpiCard unrealizedPL = new KpiCard("Unrealized P/L",
            FontAwesome.LINE_CHART, Tone.PURPLE);
    private final KpiCard winRate = new KpiCard("Win Rate",
            FontAwesome.TROPHY, Tone.GOLD);

    // Performance Summary fields
    private final Label totalReturnValue = new Label("+$0.00");
    private final Label totalReturnPercent = new Label("(+0.00%)");
    private final Label avgTradeSize = new Label("$0.00");
    private final Label mostTraded = new Label("—");

    // Holdings table
    private final ObservableList<Share> holdings = FXCollections.observableArrayList();
    private final TableView<Share> holdingsTable = new TableView<>();

    public StatsView() {
        HBox kpis = new HBox(
                totalTrades.root, realizedPL.root,
                unrealizedPL.root, winRate.root);
        kpis.getStyleClass().add("stats-kpi-row");
        for (KpiCard c : new KpiCard[] { totalTrades, realizedPL, unrealizedPL, winRate }) {
            HBox.setHgrow(c.root, Priority.ALWAYS);
            c.root.setMaxWidth(Double.MAX_VALUE);
        }

        view.getStyleClass().add("stats-view");
        view.setSpacing(16);
        view.getChildren().addAll(kpis, buildPerformanceSummary(), buildHoldingsCard());

        // Reasonable defaults so the empty state still looks meaningful
        setTotalTrades(0, 0, 0);
        setRealizedPL("+$0.00", Tone.POSITIVE);
        setUnrealizedPL("+$0.00", Tone.POSITIVE);
        setWinRate("0.0%", 0, 0);
        setTotalReturn("+$0.00", "(+0.00%)", Tone.POSITIVE);
    }

    private VBox buildPerformanceSummary() {
        FontIcon icon = new FontIcon(FontAwesome.BAR_CHART);
        icon.getStyleClass().add("perf-summary-icon");
        Label title = new Label("Performance Summary");
        title.getStyleClass().add("perf-summary-title");
        HBox header = new HBox(icon, title);
        header.getStyleClass().add("perf-summary-header");
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox columns = new HBox(
                buildSummaryColumn("Total Return",
                        wrapHBox(totalReturnValue, totalReturnPercent, 8),
                        Pos.CENTER_LEFT),
                buildSummaryColumn("Avg Trade Size", avgTradeSize, Pos.CENTER),
                buildSummaryColumn("Most Traded", mostTraded, Pos.CENTER_RIGHT));
        columns.getStyleClass().add("perf-summary-columns");

        totalReturnValue.getStyleClass().add("perf-summary-big");
        totalReturnPercent.getStyleClass().add("perf-summary-sub");
        avgTradeSize.getStyleClass().add("perf-summary-big");
        mostTraded.getStyleClass().addAll("perf-summary-big", "perf-summary-mostTraded");

        VBox card = new VBox(header, columns);
        card.getStyleClass().add("perf-summary-card");
        card.setSpacing(18);
        return card;
    }

    private VBox buildSummaryColumn(String label, Node value, Pos alignment) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("perf-summary-label");

        VBox col = new VBox(labelNode, value);
        col.getStyleClass().add("perf-summary-col");
        col.setAlignment(alignment);
        col.setSpacing(6);
        HBox.setHgrow(col, Priority.ALWAYS);
        col.setMaxWidth(Double.MAX_VALUE);
        return col;
    }

    private static HBox wrapHBox(Node a, Node b, double spacing) {
        HBox h = new HBox(a, b);
        h.setSpacing(spacing);
        h.setAlignment(Pos.BASELINE_LEFT);
        return h;
    }

    /** Card listing each current holding with per-position P/L. */
    @SuppressWarnings("unchecked")
    private VBox buildHoldingsCard() {
        FontIcon icon = new FontIcon(FontAwesome.SHOPPING_CART);
        icon.getStyleClass().add("holdings-icon");
        Label title = new Label("Current Holdings Performance");
        title.getStyleClass().add("holdings-title");
        HBox header = new HBox(icon, title);
        header.getStyleClass().add("holdings-header");
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        TableColumn<Share, Share> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        symbolCol.setCellFactory(_ -> holdingsSymbolCell());
        symbolCol.getStyleClass().add("col-h-symbol");

        TableColumn<Share, BigDecimal> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        qtyCol.setCellFactory(_ -> plainCell(v -> v.stripTrailingZeros().toPlainString()));
        qtyCol.getStyleClass().add("col-h-qty");

        TableColumn<Share, BigDecimal> avgBuyCol = new TableColumn<>("Avg Buy");
        avgBuyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPricePerShare()));
        avgBuyCol.setCellFactory(_ -> moneyCell());
        avgBuyCol.getStyleClass().add("col-h-avgbuy");

        TableColumn<Share, BigDecimal> currentCol = new TableColumn<>("Current");
        currentCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getStock().getSalesPrice()));
        currentCol.setCellFactory(_ -> moneyCell());
        currentCol.getStyleClass().add("col-h-current");

        TableColumn<Share, BigDecimal> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(c -> {
            Share s = c.getValue();
            return new ReadOnlyObjectWrapper<>(
                    s.getStock().getSalesPrice().multiply(s.getQuantity()));
        });
        valueCol.setCellFactory(_ -> moneyCell());
        valueCol.getStyleClass().add("col-h-value");

        TableColumn<Share, Share> plCol = new TableColumn<>("P/L");
        plCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        plCol.setCellFactory(_ -> profitLossCell());
        plCol.getStyleClass().add("col-h-pl");

        holdingsTable.getColumns().setAll(symbolCol, qtyCol, avgBuyCol, currentCol, valueCol, plCol);
        holdingsTable.setItems(holdings);
        holdingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        holdingsTable.setPlaceholder(new Label("You have no current holdings."));
        holdingsTable.getStyleClass().add("holdings-table");

        VBox card = new VBox(header, holdingsTable);
        card.getStyleClass().add("holdings-card");
        card.setSpacing(0);
        return card;
    }

    /** Symbol/company two-line cell for the holdings table. */
    private static TableCell<Share, Share> holdingsSymbolCell() {
        return new TableCell<>() {
            private final Label sym = new Label();
            private final Label company = new Label();
            private final VBox box = new VBox(sym, company);
            {
                sym.getStyleClass().add("holdings-row-symbol");
                company.getStyleClass().add("holdings-row-company");
                box.getStyleClass().add("holdings-symbol-cell");
                box.setSpacing(2);
            }

            @Override
            protected void updateItem(Share share, boolean empty) {
                super.updateItem(share, empty);
                if (empty || share == null) {
                    setGraphic(null);
                    return;
                }
                sym.setText(share.getStock().getSymbol());
                company.setText(share.getStock().getCompany());
                setGraphic(box);
            }
        };
    }

    /** P/L cell: signed $ amount on top, signed percent in parentheses below. */
    private static TableCell<Share, Share> profitLossCell() {
        return new TableCell<>() {
            private final Label amount = new Label();
            private final Label percent = new Label();
            private final VBox box = new VBox(amount, percent);
            {
                box.getStyleClass().add("holdings-pl-cell");
                amount.getStyleClass().add("holdings-pl-amount");
                percent.getStyleClass().add("holdings-pl-percent");
                box.setAlignment(Pos.CENTER_RIGHT);
                box.setSpacing(2);
            }

            @Override
            protected void updateItem(Share share, boolean empty) {
                super.updateItem(share, empty);
                box.getStyleClass().removeAll("pl--positive", "pl--negative");
                if (empty || share == null) {
                    setGraphic(null);
                    return;
                }
                BigDecimal avgBuy = share.getPricePerShare();
                BigDecimal current = share.getStock().getSalesPrice();
                BigDecimal qty = share.getQuantity();
                BigDecimal pl = current.subtract(avgBuy).multiply(qty);
                int sign = pl.signum();
                BigDecimal pct = avgBuy.signum() == 0 ? BigDecimal.ZERO
                        : current.subtract(avgBuy)
                              .divide(avgBuy, 4, RoundingMode.HALF_UP)
                              .movePointRight(2);

                String sym = sign >= 0 ? "+" : "-";
                amount.setText(sym + "$" + pl.abs().setScale(2, RoundingMode.HALF_UP).toPlainString());
                percent.setText("(" + (pct.signum() >= 0 ? "+" : "")
                        + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%)");
                box.getStyleClass().add(sign >= 0 ? "pl--positive" : "pl--negative");
                setGraphic(box);
            }
        };
    }

    private static <T> TableCell<Share, T> plainCell(java.util.function.Function<T, String> format) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : format.apply(value));
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

    public VBox getView() { return view; }

    // ----- Setters for a controller to push data -----

    public void setTotalTrades(int total, int buys, int sells) {
        totalTrades.value.setText(String.valueOf(total));
        Label buysLabel = new Label("↑ " + buys + " buys");
        buysLabel.getStyleClass().addAll("kpi-trend", "kpi-trend--positive");
        Label sellsLabel = new Label("↓ " + sells + " sells");
        sellsLabel.getStyleClass().addAll("kpi-trend", "kpi-trend--negative");
        HBox row = new HBox(buysLabel, sellsLabel);
        row.setSpacing(14);
        totalTrades.setSubline(row);
    }

    public void setRealizedPL(String text, Tone tone) {
        retone(realizedPL.value, tone);
        realizedPL.value.setText(text);
        Label sub = new Label("From completed trades");
        sub.getStyleClass().add("kpi-sub");
        realizedPL.setSubline(sub);
    }

    public void setUnrealizedPL(String text, Tone tone) {
        retone(unrealizedPL.value, tone);
        unrealizedPL.value.setText(text);
        Label sub = new Label("Current holdings");
        sub.getStyleClass().add("kpi-sub");
        unrealizedPL.setSubline(sub);
    }

    public void setWinRate(String text, int wins, int losses) {
        winRate.value.setText(text);
        Label winsLabel = new Label(wins + " wins");
        winsLabel.getStyleClass().addAll("kpi-trend", "kpi-trend--positive");
        Label lossesLabel = new Label(losses + " losses");
        lossesLabel.getStyleClass().addAll("kpi-trend", "kpi-trend--negative");
        HBox row = new HBox(winsLabel, lossesLabel);
        row.setSpacing(14);
        winRate.setSubline(row);
    }

    public void setTotalReturn(String value, String percent, Tone tone) {
        totalReturnValue.setText(value);
        totalReturnPercent.setText(percent);
        retone(totalReturnValue, tone);
        retone(totalReturnPercent, tone);
    }

    public void setAvgTradeSize(String value) { avgTradeSize.setText(value); }
    public void setMostTraded(String value)   { mostTraded.setText(value); }

    /** Replace the holdings list shown in the "Current Holdings Performance" card. */
    public void setHoldings(java.util.List<Share> shares) {
        holdings.setAll(shares);
    }

    public ObservableList<Share> getHoldings() { return holdings; }

    private static void retone(Label label, Tone tone) {
        label.getStyleClass().removeIf(s -> s.startsWith("tone-"));
        label.getStyleClass().add("tone-" + tone.name().toLowerCase());
    }

    // ----- Card scaffolding -----

    /** A single KPI card: icon tile + label, big value, swappable subline. */
    private static class KpiCard {
        final VBox root = new VBox();
        final Label value = new Label("0");
        private final VBox sublineSlot = new VBox();

        KpiCard(String labelText, Ikon icon, Tone iconTone) {
            FontIcon iconNode = new FontIcon(icon);
            iconNode.getStyleClass().add("kpi-icon");

            StackPane iconBox = new StackPane(iconNode);
            iconBox.getStyleClass().addAll("kpi-icon-box", "kpi-icon-box--" + iconTone.name().toLowerCase());

            Label label = new Label(labelText);
            label.getStyleClass().add("kpi-label");

            VBox labelAndValue = new VBox(label, value);
            labelAndValue.setSpacing(2);
            value.getStyleClass().add("kpi-value");

            HBox top = new HBox(iconBox, labelAndValue);
            top.getStyleClass().add("kpi-top");
            top.setSpacing(14);
            top.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            root.getChildren().addAll(top, spacer, sublineSlot);
            root.getStyleClass().add("kpi-card");
            root.setSpacing(12);
        }

        void setSubline(Node node) {
            sublineSlot.getChildren().setAll(node);
        }
    }
}
