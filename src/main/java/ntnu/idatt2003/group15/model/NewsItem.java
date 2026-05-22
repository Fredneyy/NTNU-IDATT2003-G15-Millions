package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.time.Instant;

public class NewsItem {

  private final StockSectors sector;
  private BigDecimal changePercent;
  private final String headline;
  private final BigDecimal volatility;
  private int durationUpdates;
  private final Instant when;
  private boolean appliedChange;

  public NewsItem(
      StockSectors sector,
      BigDecimal changePercent,
      String headline,
      BigDecimal volatility,
      int durationUpdates,
      Instant when,
      boolean appliedChange
  ) {
    this.sector = sector;
    this.changePercent = changePercent;
    this.headline = headline;
    this.volatility = volatility;
    this.durationUpdates = durationUpdates;
    this.when = when;
    this.appliedChange = appliedChange;
  }

  /** Decrements the remaining duration by one update. */
  public void reduceDuration() {
    if (durationUpdates > 0) {
      durationUpdates--;
    }
  }

  /** Returns true when the news effect has fully expired. */
  public boolean isExpired() {
    return durationUpdates <= 0;
  }

  /** Footer text shown in the popup. Derived from the structured fields. */
  public String footerText() {
    if (durationUpdates <= 0) return null;
    return "Volatility increased for " + durationUpdates + " updates";
  }

  /** Plain informational item (e.g. the welcome message). Skipped by feed listeners. */
  public static NewsItem info(String headline, String message) {
    return new NewsItem(null, null,
        headline, null, 0, Instant.now(), false);
  }

  public void setAppliedChange(boolean appliedChange) {
    this.appliedChange = appliedChange;
  }

  public void setChangePercent(BigDecimal changePercent) {
    this.changePercent = changePercent;
  }

  public StockSectors sector() { return sector; }
  public BigDecimal changePercent() { return changePercent; }
  public String headline() { return headline; }
  public BigDecimal volatility() { return volatility; }
  public int durationUpdates() { return durationUpdates; }
  public Instant when() { return when; }
  public boolean appliedChange() { return appliedChange; }
}