package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.news.NewsItem;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * News tab content: scrollable list of recent market events. Each row shows
 * sentiment (bullish/bearish), the affected ticker, percent change, headline,
 * description, and meta (volatility / duration / type).
 */
public class NewsFeedView {

    private final VBox view = new VBox();
    private final Label subtitle = new Label("0 events recorded");
    private final VBox rowsContainer = new VBox();

    private final ObservableList<NewsItem> events = FXCollections.observableArrayList();
    private final HashMap<NewsItem, NewsRow> rows = new HashMap<>();

    public NewsFeedView() {
        view.getStyleClass().add("news-card");
        view.getChildren().addAll(buildHeader(), buildBody());

        events.addListener((ListChangeListener<NewsItem>) change -> {
            if (change.next()) {
                if (change.wasAdded()) {
                    for (NewsItem item : change.getAddedSubList()) {
                        NewsRow newsRow =  new NewsRow(item, false);
                        rows.put(item, newsRow);
                        rowsContainer.getChildren().add(newsRow.root);
                    }
                }
                if (change.wasRemoved()) {
                    for (NewsItem item : change.getRemoved()) {
                        rowsContainer.getChildren().remove(rows.get(item).root);
                        rows.remove(item);
                    }
                }
            }
        });
        subtitle.textProperty().bind(Bindings.createStringBinding(
                () -> events.size() + (events.size() == 1
                        ? " event recorded"
                        : " events recorded"),
                events));
    }

    private VBox buildHeader() {
        Label title = new Label("Market News Feed");
        title.getStyleClass().add("news-header-title");
        subtitle.getStyleClass().add("news-header-subtitle");

        VBox header = new VBox(title, subtitle);
        header.setSpacing(4);
        header.getStyleClass().add("news-header");
        return header;
    }

    private VBox buildBody() {
        rowsContainer.getStyleClass().add("news-rows");
        ScrollPane scroller = new ScrollPane(rowsContainer);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("news-scroll");
        VBox.setVgrow(scroller, Priority.ALWAYS);
        VBox body = new VBox(scroller);
        body.getStyleClass().add("news-body");
        return body;
    }

    public VBox getView() { return view; }
    public ObservableList<NewsItem> getEvents() { return events; }
    public void setEvents(ObservableList<NewsItem> items) { Bindings.bindContent(events, items); }

    private static final class NewsRow {
        final VBox root = new VBox();

        NewsRow(NewsItem ev, boolean withDivider) {
            BigDecimal rawPct = ev.changePercent() == null ? BigDecimal.ZERO : ev.changePercent();
            boolean bullish = rawPct.compareTo(BigDecimal.ONE) >= 0;

            // Trend-arrow icon tile (green up / red down)
            FontIcon arrow = new FontIcon(bullish ? FontAwesome.LINE_CHART : FontAwesome.AREA_CHART);
            arrow.getStyleClass().add("news-row-icon");
            StackPane iconBox = new StackPane(arrow);
            iconBox.getStyleClass().addAll("news-row-icon-box",
                    bullish ? "news-row-icon-box--bullish" : "news-row-icon-box--bearish");

            // Top badges row: SECTOR  ±X.X%  [BULLISH | BEARISH]  ............ clock + ago
            Label symbolBadge = new Label(ev.sector() == null ? "" : ev.sector().getLabel());
            symbolBadge.getStyleClass().addAll("news-badge", "news-badge--symbol");

            BigDecimal pct = rawPct.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
            String pctText = (pct.signum() >= 0 ? "+" : "") + pct.toPlainString() + "%";
            Label pctLabel = new Label(pctText);
            pctLabel.getStyleClass().addAll("news-row-percent",
                    bullish ? "news-row-percent--bullish" : "news-row-percent--bearish");

            Label sentimentBadge = new Label(bullish ? "BULLISH" : "BEARISH");
            sentimentBadge.getStyleClass().addAll("news-badge", "news-badge--sentiment",
                    bullish ? "news-badge--bullish" : "news-badge--bearish");

            FontIcon clock = new FontIcon(FontAwesome.CLOCK_O);
            clock.getStyleClass().add("news-row-clock");
            Label ago = new Label(relativeTime(ev.when()));
            ago.getStyleClass().add("news-row-ago");
            HBox agoRow = new HBox(clock, ago);
            agoRow.setSpacing(6);
            agoRow.setAlignment(Pos.CENTER_RIGHT);

            HBox badges = new HBox(symbolBadge, pctLabel, sentimentBadge);
            badges.setSpacing(12);
            badges.setAlignment(Pos.CENTER_LEFT);

            Region badgeSpacer = new Region();
            HBox.setHgrow(badgeSpacer, Priority.ALWAYS);
            HBox topRow = new HBox(badges, badgeSpacer, agoRow);
            topRow.getStyleClass().add("news-row-top");
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label description = new Label(ev.headline() == null ? "" : ev.headline());
            description.getStyleClass().add("news-row-description");
            description.setWrapText(true);

            // Meta row
            BigDecimal vol = ev.volatility() == null ? BigDecimal.ZERO : ev.volatility();
            HBox meta = new HBox(
                    metaPair("Volatility:", vol.stripTrailingZeros().toPlainString() + "x"),
                    metaPair("Duration:", ev.durationUpdates() + " updates")
            );
            meta.setSpacing(28);
            meta.getStyleClass().add("news-row-meta");

            VBox center = new VBox(topRow, description, meta);
            center.setSpacing(8);
            HBox.setHgrow(center, Priority.ALWAYS);
            center.setMaxWidth(Double.MAX_VALUE);

            HBox content = new HBox(iconBox, center);
            content.setSpacing(16);
            content.setAlignment(Pos.TOP_LEFT);
            content.getStyleClass().add("news-row-content");

            root.getChildren().add(content);
            root.getStyleClass().add("news-row");
            if (withDivider) {
                Region divider = new Region();
                divider.getStyleClass().add("news-row-divider");
                root.getChildren().add(divider);
            }
        }

        private static HBox metaPair(String label, String value) {
            Label l = new Label(label);
            l.getStyleClass().add("news-meta-label");
            Label v = new Label(value);
            v.getStyleClass().add("news-meta-value");
            HBox row = new HBox(l, v);
            row.setSpacing(6);
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        }

        private static String relativeTime(Instant when) {
            if (when == null) return "";
            Duration d = Duration.between(when, Instant.now());
            long seconds = d.getSeconds();
            if (seconds < 30)      return "Just now";
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
}
