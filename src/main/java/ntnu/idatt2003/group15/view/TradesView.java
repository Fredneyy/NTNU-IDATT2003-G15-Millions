package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Trades tab content: a card listing recent transactions, each with a BUY/SELL
 * badge, symbol badge, total amount, and "X ago" relative timestamp.
 */
public class TradesView {

    public enum TradeType { BUY, SELL }

    /** View-model record decoupled from domain {@code Transaction} so a controller
     *  can map between them without coupling the view to weeks/calculators. */
    public record TradeRecord(
            TradeType type,
            String symbol,
            String company,
            BigDecimal quantity,
            BigDecimal price,
            Instant when
    ) {
        public BigDecimal total() {
            return price.multiply(quantity);
        }
    }

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy, hh:mm a");

    private final VBox view = new VBox();
    private final Label subtitle = new Label("0 transactions recorded");
    private final Label totalVolume = new Label("$0.00");
    private final VBox rowsContainer = new VBox();
    private final FilteredList<TradeRecord> filteredTradeRecords;
    private final HashMap<TradeRecord, TradeRow> tradeRows = new HashMap<>();

    private final ObservableList<TradeRecord> trades = FXCollections.observableArrayList();

    public TradesView() {

        filteredTradeRecords = new FilteredList<>(trades, _ -> true);

        view.getStyleClass().add("trades-card");

        view.getChildren().addAll(buildHeader(), buildBody());

        trades.addListener((ListChangeListener<TradeRecord>) _ -> rebuildRows());
        bindHeaderSummary();
        rebuildRows();
    }

    private HBox buildHeader() {
        FontIcon cart = new FontIcon(FontAwesome.SHOPPING_CART);
        cart.getStyleClass().add("trades-header-icon");
        StackPane iconBox = new StackPane(cart);
        iconBox.getStyleClass().add("trades-header-icon-box");

        Label title = new Label("Transaction History");
        title.getStyleClass().add("trades-header-title");
        subtitle.getStyleClass().add("trades-header-subtitle");

        VBox titleBox = new VBox(title, subtitle);
        titleBox.setSpacing(2);

        HBox left = new HBox(iconBox, titleBox);
        left.getStyleClass().add("trades-header-left");
        left.setSpacing(14);
        left.setAlignment(Pos.CENTER_LEFT);

        Label caption = new Label("Total Volume");
        caption.getStyleClass().add("trades-header-caption");
        totalVolume.getStyleClass().add("trades-header-total");
        VBox totalBox = new VBox(caption, totalVolume);
        totalBox.setSpacing(2);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(left, spacer, totalBox);
        header.getStyleClass().add("trades-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildBody() {
        rowsContainer.getStyleClass().add("trades-rows");
        ScrollPane scroller = new ScrollPane(rowsContainer);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("trades-scroll");
        VBox.setVgrow(scroller, Priority.ALWAYS);
        VBox body = new VBox(scroller);
        body.getStyleClass().add("trades-body");
        return body;
    }

    private void bindHeaderSummary() {
        subtitle.textProperty().bind(Bindings.createStringBinding(
                () -> trades.size() + (trades.size() == 1
                        ? " transaction recorded"
                        : " transactions recorded"),
                trades));
        totalVolume.textProperty().bind(Bindings.createStringBinding(
                () -> "$" + totalVolume().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                trades));
    }

    private BigDecimal totalVolume() {
        BigDecimal sum = BigDecimal.ZERO;
        for (TradeRecord t : trades) sum = sum.add(t.total().abs());
        return sum;
    }

    private void rebuildRows() {
        List<TradeRecord> snapshot = new ArrayList<>(trades);

        for (TradeRecord tradeRecord : snapshot) {
            if (!tradeRows.containsKey(tradeRecord)) {
                TradeRow tradeRow = new TradeRow(tradeRecord, true);
                tradeRows.put(tradeRecord, tradeRow);
                rowsContainer.getChildren().add(tradeRow.getView());
            }
        }
    }

    public VBox getView() { return view; }
    public ObservableList<TradeRecord> getTrades() { return trades; }
    public void setTrades(List<TradeRecord> records) { trades.setAll(records); }

    private static final class TradeRow {
        final VBox root = new VBox();

        TradeRow(TradeRecord t, boolean withDivider) {
            boolean buy = t.type() == TradeType.BUY;

            // Left icon tile
            FontIcon arrow = new FontIcon(buy ? FontAwesome.LINE_CHART : FontAwesome.AREA_CHART);
            arrow.getStyleClass().add("trade-row-icon");
            StackPane iconBox = new StackPane(arrow);
            iconBox.getStyleClass().addAll("trade-row-icon-box",
                    buy ? "trade-row-icon-box--buy" : "trade-row-icon-box--sell");

            // Type + symbol badges
            Label typeBadge = new Label(buy ? "BUY" : "SELL");
            typeBadge.getStyleClass().addAll("trade-badge", "trade-badge--type",
                    buy ? "trade-badge--buy" : "trade-badge--sell");

            Label symbolBadge = new Label(t.symbol());
            symbolBadge.getStyleClass().addAll("trade-badge", "trade-badge--symbol");

            HBox badges = new HBox(typeBadge, symbolBadge);
            badges.setSpacing(8);
            badges.setAlignment(Pos.CENTER_LEFT);

            Label company = new Label(t.company());
            company.getStyleClass().add("trade-row-company");

            // Metadata row (quantity, price, date)
            HBox meta = new HBox(
                    metaPair("Quantity:", t.quantity().stripTrailingZeros().toPlainString()),
                    metaPair("Price:", "$" + t.price().setScale(2, RoundingMode.HALF_UP).toPlainString()),
                    metaPair("Date:", DATE_FMT.format(
                            LocalDateTime.ofInstant(t.when(), ZoneId.systemDefault())))
            );
            meta.setSpacing(28);
            meta.getStyleClass().add("trade-row-meta");

            VBox center = new VBox(badges, company);
            center.setSpacing(8);

            // Right-side amount + relative time
            BigDecimal abs = t.total().abs().setScale(2, RoundingMode.HALF_UP);
            Label amount = new Label((buy ? "-" : "+") + "$" + abs.toPlainString());
            amount.getStyleClass().addAll("trade-row-amount",
                    buy ? "trade-row-amount--out" : "trade-row-amount--in");

            FontIcon clock = new FontIcon(FontAwesome.CLOCK_O);
            clock.getStyleClass().add("trade-row-clock");
            Label ago = new Label(relativeTime(t.when()));
            ago.getStyleClass().add("trade-row-ago");
            HBox agoRow = new HBox(clock, ago);
            agoRow.setSpacing(6);
            agoRow.setAlignment(Pos.CENTER_RIGHT);

            VBox right = new VBox(amount, agoRow);
            right.setSpacing(4);
            right.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topRow = new HBox(iconBox, center, spacer, right);
            topRow.setSpacing(16);
            topRow.setAlignment(Pos.CENTER_LEFT);
            topRow.getStyleClass().add("trade-row-top");

            VBox content = new VBox(topRow, meta);
            content.setSpacing(12);
            content.getStyleClass().add("trade-row-content");

            root.getChildren().add(content);
            root.getStyleClass().add("trade-row");
            if (withDivider) {
                Region divider = new Region();
                divider.getStyleClass().add("trade-row-divider");
                root.getChildren().add(divider);
            }
        }

        public VBox getView() {
            return root;
        }

        public void setDisabled() {
            root.setManaged(false);
            root.setVisible(false);
        }

        public void setVisible() {
            root.setVisible(true);
            root.setManaged(true);
        }

        private static HBox metaPair(String label, String value) {
            Label l = new Label(label);
            l.getStyleClass().add("trade-meta-label");
            Label v = new Label(value);
            v.getStyleClass().add("trade-meta-value");
            HBox row = new HBox(l, v);
            row.setSpacing(6);
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        }

        private static String relativeTime(Instant when) {
            if (when == null) return "";
            Duration d = Duration.between(when, Instant.now());
            long seconds = d.getSeconds();
            if (seconds < 60)      return seconds + "s ago";
            long minutes = seconds / 60;
            if (minutes < 60)      return minutes + "m ago";
            long hours = minutes / 60;
            if (hours < 24)        return hours + "h ago";
            long days = hours / 24;
            if (days < 30)         return days + "d ago";
            long months = days / 30;
            if (months < 12)       return months + "mo ago";
            return (days / 365) + "y ago";
        }
    }

    /** Filter table rows by symbol/company substring (case insensitive). Empty resets. */
    public void setSearchFilter(String query) {
        if (query == null || query.isBlank()) {
            filteredTradeRecords.setPredicate(_ -> true);
        } else {
            String q = query.trim().toLowerCase();
            filteredTradeRecords.setPredicate(s ->
                s.company().toLowerCase().contains(q)
                    || s.symbol().toLowerCase().contains(q));
        }

        // Show/hide rows to match the current predicate
        for (TradeRecord record : trades) {
            TradeRow row = tradeRows.get(record);
            if (row == null) continue;
            if (filteredTradeRecords.contains(record)) {
                row.setVisible();
            } else {
                row.setDisabled();
            }
        }
    }
}
