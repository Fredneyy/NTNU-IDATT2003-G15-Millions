package ntnu.idatt2003.group15.controller;

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

    /** Plain data — title + body shown in each notification. */
    public record NewsItem(String title, String message) {}

    private static final Duration TICK_INTERVAL = Duration.seconds(10);
    /** How long each individual notification stays visible. Long enough that
     *  several stack on screen at once. */
    private static final Duration ITEM_LIFETIME = Duration.seconds(25);

    private final StackPane root;
    private final NewsContainer container = new NewsContainer();

    /** Dummy headlines that cycle indefinitely. */
    private final List<NewsItem> dummies = List.of(
            new NewsItem("AMZN -8.0% — Bearish",
                    "Amazon's quarterly results fall short of analyst predictions."),
            new NewsItem("SOFI +25.0% — Bullish",
                    "SoFi Technologies unveils groundbreaking AI lending platform."),
            new NewsItem("NFLX +12.0% — Bullish",
                    "Netflix forms strategic partnership in emerging markets."),
            new NewsItem("NVDA +4.5% — Bullish",
                    "NVIDIA reveals next-gen GPU lineup ahead of schedule."),
            new NewsItem("TSLA -6.2% — Bearish",
                    "Tesla delivery numbers come in below analyst consensus."),
            new NewsItem("GOOGL -20.0% — Bearish",
                    "Antitrust ruling forces Alphabet to spin off ad business."),
            new NewsItem("MSFT +3.8% — Bullish",
                    "Microsoft Azure secures multi-year defense contract."),
            new NewsItem("AAPL +1.6% — Neutral",
                    "Apple announces stock split alongside record buyback program.")
    );

    private Timeline timer;
    private int index = 0;

    public NewsController(StackPane root) {
        this.root = root;
    }

    /** Mount the container into the root and start the 10s news loop. */
    public void start() {
        if (root == null) return;
        container.mountIn(root);
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(TICK_INTERVAL, _ -> showNext()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        // Fire the first one straight away so the player doesn't wait 10s.
        showNext();
    }

    /** Halt the loop and tear down any visible notifications. */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        container.unmount();
    }

    /** Push a one-off notification onto the stack outside the rotation. */
    public void push(String title, String message) {
        NewsDialog dialog = new NewsDialog(ITEM_LIFETIME);
        dialog.setText(title, message);
        dialog.showIn(container);
    }

    private void showNext() {
        if (dummies.isEmpty()) return;
        NewsItem item = dummies.get(index % dummies.size());
        index++;
        push(item.title(), item.message());
    }

    public NewsContainer getContainer() { return container; }
}
