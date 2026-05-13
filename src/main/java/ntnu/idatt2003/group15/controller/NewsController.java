package ntnu.idatt2003.group15.controller;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.NewsItem;
import ntnu.idatt2003.group15.view.NewsContainer;
import ntnu.idatt2003.group15.view.NewsDialog;

public class NewsController {

    private static final Duration TICK_INTERVAL = Duration.minutes(1);
    private static final Duration TICK_STDDEV = Duration.seconds(15);
    private static final Duration MIN_TICK = Duration.seconds(5);
    private static final Duration ITEM_LIFETIME = Duration.seconds(25);

    private final StackPane root;
    private final NewsContainer container = new NewsContainer();
    private final Random random = new Random();
    private Timeline timer;
    private int index = 0;
    private Consumer<NewsItem> onEmitted = _ -> {};

    public NewsController(StackPane root) {
        this.root = root;
    }

    public void setOnNewsEmitted(Consumer<NewsItem> listener) {
        this.onEmitted = Objects.requireNonNull(listener);
    }

    public void start() {
        if (root == null) return;
        container.mountIn(root);
        if (timer != null) timer.stop();
        scheduleNext();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        container.unmount();
    }

    public void push(String title, String message) {
        pushItem(NewsItem.info(title, message));
    }

    public void pushItem(NewsItem item) {
        NewsItem stamped = ensureStamped(item);

        NewsDialog dialog = new NewsDialog(ITEM_LIFETIME);
        dialog.setSentiment(stamped.sentiment());
        dialog.setSymbol(stamped.symbol());
        dialog.setChangePercent(stamped.changePercent());
        dialog.setText(stamped.title(), stamped.message());
        dialog.setFooter(stamped.footerText());
        dialog.showIn(container);

        onEmitted.accept(stamped);
    }

    private static NewsItem ensureStamped(NewsItem item) {
        if (item.when() != null) return item;
        return new NewsItem(
            item.sentiment(), item.symbol(), item.changePercent(), item.drift(),
            item.title(), item.message(), item.volatility(),
            item.durationUpdates(), item.type(), Instant.now()
        );
    }

    /**
     * Schedule the next tick using a Gaussian-distributed delay centered on
     * TICK_INTERVAL with TICK_STDDEV spread, floored at MIN_TICK.
     */
    private void scheduleNext() {
        double mean = TICK_INTERVAL.toMillis();
        double stddev = TICK_STDDEV.toMillis();
        double jittered = mean + random.nextGaussian() * stddev;
        double clamped = Math.max(MIN_TICK.toMillis(), jittered);

        Duration nextDelay = Duration.millis(clamped);

        timer = new Timeline(new KeyFrame(nextDelay, _ -> {
            showNext();
            scheduleNext(); // self-reschedule with a new random delay
        }));
        timer.setCycleCount(1);
        timer.play();
    }

    private void showNext() {
        // your existing logic
    }

    public NewsContainer getContainer() { return container; }
}