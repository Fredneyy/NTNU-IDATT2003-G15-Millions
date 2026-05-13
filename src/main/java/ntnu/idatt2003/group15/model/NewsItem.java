package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.view.NewsDialog;

import java.math.BigDecimal;
import java.time.Instant;

public record NewsItem(
    NewsDialog.Sentiment sentiment,
    String symbol,
    BigDecimal changePercent,
    BigDecimal drift,
    String title,
    String message,
    BigDecimal volatility,
    int durationUpdates,
    String type,
    Instant when
) {
  /** Footer text shown in the popup. Derived from the structured fields. */
  public String footerText() {
    if (durationUpdates <= 0) return null;
    return "Volatility increased for " + durationUpdates + " updates";
  }

  /** Plain informational item (e.g. the welcome message). Skipped by feed listeners. */
  public static NewsItem info(String title, String message) {
    return new NewsItem(NewsDialog.Sentiment.NEUTRAL, null, null, null,
        title, message, null, 0, null, Instant.now());
  }
}