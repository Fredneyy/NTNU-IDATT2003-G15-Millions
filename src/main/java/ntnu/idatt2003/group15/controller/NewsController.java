package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.view.NewsContainer;
import ntnu.idatt2003.group15.view.NewsDialog;

/**
 * Drives an in-game stream of breaking news. Owns a {@link NewsContainer}
 * mounted top-right in the root StackPane and pushes a new {@link NewsDialog}
 * onto it every 10 seconds. Newest notifications appear at the top of the
 * stack and visually push older ones downward.
 *
 * For now the news come from a fixed dummy list; a real implementation would
 * pull from {@code Exchange} / {@code PriceEvent} streams.
 */
public class NewsController {

    /** Rich payload for one news notification. */
    public record NewsItem(
            NewsDialog.Sentiment sentiment,
            String symbol,
            BigDecimal changePercent,
            String title,
            String message,
            String footer
    ) {
        public static NewsItem info(String title, String message) {
            return new NewsItem(NewsDialog.Sentiment.NEUTRAL, null, null, title, message, null);
        }
    }

    private static final Duration TICK_INTERVAL = Duration.minutes(1);
    /** How long each individual notification stays visible. */
    private static final Duration ITEM_LIFETIME = Duration.seconds(25);

    private final StackPane root;
    private final NewsContainer container = new NewsContainer();

    /** Dummy headlines that cycle indefinitely. */
    private final List<NewsItem> dummies = List.of(
            new NewsItem(NewsDialog.Sentiment.BULLISH, "HOOD", new BigDecimal("20.0"),
                    "Robinhood Markets: Major Scientific Breakthrough",
                    "Research team achieves milestone that analysts call \"game-changing.\"",
                    "Volatility increased for 16 updates"),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "NFLX", new BigDecimal("-11.0"),
                    "Netflix Inc.: Launch Disaster",
                    "Highly anticipated product plagued by defects and poor reviews.",
                    "Volatility increased for 19 updates"),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "AMZN", new BigDecimal("-8.0"),
                    "Amazon.com Inc.: Earnings Miss Expectations",
                    "Quarterly results fall short of analyst predictions, citing market headwinds.",
                    "Volatility increased for 22 updates"),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "SOFI", new BigDecimal("25.0"),
                    "SoFi Technologies: Revolutionary Product Announced",
                    "Company unveils groundbreaking technology that could transform the industry.",
                    "Volatility increased for 18 updates"),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "NVDA", new BigDecimal("4.5"),
                    "NVIDIA: Next-Gen GPU Reveal Ahead of Schedule",
                    "Performance benchmarks leak online, exceeding analyst expectations.",
                    "Volatility increased for 11 updates"),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "TSLA", new BigDecimal("-6.2"),
                    "Tesla Inc.: Delivery Numbers Disappoint",
                    "Quarterly delivery figures fall short of consensus, raising demand concerns.",
                    "Volatility increased for 14 updates"),
            new NewsItem(NewsDialog.Sentiment.BEARISH, "GOOGL", new BigDecimal("-20.0"),
                    "Alphabet Inc.: Antitrust Ruling Forces Spin-off",
                    "Court orders ad business to be divested within 18 months.",
                    "Volatility increased for 25 updates"),
            new NewsItem(NewsDialog.Sentiment.BULLISH, "MSFT", new BigDecimal("3.8"),
                    "Microsoft: Multi-Year Defense Contract Secured",
                    "Azure wins flagship cloud deal valued at $12 billion over five years.",
                    "Volatility increased for 9 updates")
    );

    private Timeline timer;
    private int index = 0;

    public NewsController(StackPane root) {
        this.root = root;
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
        NewsDialog dialog = new NewsDialog(ITEM_LIFETIME);
        dialog.setSentiment(item.sentiment());
        dialog.setSymbol(item.symbol());
        dialog.setChangePercent(item.changePercent());
        dialog.setText(item.title(), item.message());
        dialog.setFooter(item.footer());
        dialog.showIn(container);
    }

    private void showNext() {
        if (dummies.isEmpty()) return;
        NewsItem item = dummies.get(index % dummies.size());
        index++;
        pushItem(item);
    }

    public NewsContainer getContainer() { return container; }
}
