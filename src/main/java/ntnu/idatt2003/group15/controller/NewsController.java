package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.NewsItem;
import ntnu.idatt2003.group15.view.NewsContainer;
import ntnu.idatt2003.group15.view.NewsDialog;

/**
 * Drives an in-game stream of breaking news. Owns a {@link NewsContainer}
 * mounted top-right in the root StackPane and pushes a new {@link NewsDialog}
 * onto it every {@link #TICK_INTERVAL}. Newest notifications appear at the top
 * of the stack and visually push older ones downward.
 *
 * Listeners can subscribe via {@link #setOnNewsEmitted(Consumer)} to be
 * notified when a fresh item fires — used by GameView to mirror events into
 * the "Market News Feed" tab.
 *
 * For now the news come from a fixed dummy list; a real implementation would
 * pull from {@code Exchange} / {@code PriceEvent} streams.
 */
public class NewsController {

    private static final Duration TICK_INTERVAL = Duration.minutes(1);
    /** How long each individual notification stays visible. */
    private static final Duration ITEM_LIFETIME = Duration.seconds(25);

    private final StackPane root;
    private final NewsContainer container = new NewsContainer();

    /** Dummy headlines that cycle indefinitely. */
    private final List<NewsItem> dummies = List.of(
            new NewsItem(NewsDialog.Sentiment.BULLISH, "HOOD", new BigDecimal("20.0"), null,
                    "Robinhood Markets: Major Scientific Breakthrough",
                    "Research team achieves milestone that analysts call \"game-changing.\"",
                    new BigDecimal("1.6"), 16, "Breakthrough", null),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "NFLX", new BigDecimal("-11.0"), null,
                    "Netflix Inc.: Launch Disaster",
                    "Highly anticipated product plagued by defects and poor reviews.",
                    new BigDecimal("1.4"), 19, "Product Issue", null),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "AMZN", new BigDecimal("-8.0"), null,
                    "Amazon.com Inc.: Earnings Miss Expectations",
                    "Quarterly results fall short of analyst predictions, citing market headwinds.",
                    new BigDecimal("1.6"), 22, "Earnings Miss", null),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "SOFI", new BigDecimal("25.0"), null,
                    "SoFi Technologies: Revolutionary Product Announced",
                    "Company unveils groundbreaking technology that could transform the industry.",
                    new BigDecimal("1.8"), 18, "Breakthrough", null),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "NVDA", new BigDecimal("4.5"), null,
                    "NVIDIA: Next-Gen GPU Reveal Ahead of Schedule",
                    "Performance benchmarks leak online, exceeding analyst expectations.",
                    new BigDecimal("1.2"), 11, "Product Launch", null),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "TSLA", new BigDecimal("-6.2"), null,
                    "Tesla Inc.: Delivery Numbers Disappoint",
                    "Quarterly delivery figures fall short of consensus, raising demand concerns.",
                    new BigDecimal("1.3"), 14, "Earnings Miss", null),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "GOOGL", new BigDecimal("-20.0"), null,
                    "Alphabet Inc.: Antitrust Ruling Forces Spin-off",
                    "Court orders ad business to be divested within 18 months.",
                    new BigDecimal("1.9"), 25, "Regulatory", null),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "MSFT", new BigDecimal("3.8"), null,
                    "Microsoft: Multi-Year Defense Contract Secured",
                    "Azure wins flagship cloud deal valued at $12 billion over five years.",
                    new BigDecimal("1.1"), 9, "Partnership", null)
    );

    private Timeline timer;
    private int index = 0;
    private Consumer<NewsItem> onEmitted = _ -> {};

    public NewsController(StackPane root) {
        this.root = root;
    }

    /** Register a listener invoked every time a news item is pushed. */
    public void setOnNewsEmitted(Consumer<NewsItem> listener) {
        this.onEmitted = listener == null ? _ -> {} : listener;
    }

    /** Mount the container into the root and start the news loop. */
    public void start() {
        if (root == null) return;
        container.mountIn(root);
        if (timer != null) timer.stop();
        // First news event fires after one full TICK_INTERVAL — no immediate showNext().
        timer = new Timeline(new KeyFrame(TICK_INTERVAL, _ -> showNext()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    /** Halt the loop and tear down any visible notifications. */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        container.unmount();
    }

    /** Plain-info notification (e.g. the welcome message) — no symbol/percent/footer. */
    public void push(String title, String message) {
        pushItem(NewsItem.info(title, message));
    }

    /** Push a fully populated rich notification onto the stack. */
    public void pushItem(NewsItem item) {
        // Stamp emission time so feed listeners can render "X seconds ago".
        NewsItem stamped = item.when() != null
                ? item
                : new NewsItem(item.sentiment(), item.symbol(), item.changePercent(), item.drift(),
                        item.title(), item.message(), item.volatility(),
                        item.durationUpdates(), item.type(), Instant.now());

        NewsDialog dialog = new NewsDialog(ITEM_LIFETIME);
        dialog.setSentiment(stamped.sentiment());
        dialog.setSymbol(stamped.symbol());
        dialog.setChangePercent(stamped.changePercent());
        dialog.setText(stamped.title(), stamped.message());
        dialog.setFooter(stamped.footerText());
        dialog.showIn(container);

        onEmitted.accept(stamped);
    }

    private void showNext() {
        if (dummies.isEmpty()) return;
        NewsItem item = dummies.get(index % dummies.size());
        index++;
        pushItem(item);
    }

    public NewsContainer getContainer() { return container; }
}
