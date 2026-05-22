package ntnu.idatt2003.group15.model.news;

import java.math.BigDecimal;
import java.time.Instant;
import ntnu.idatt2003.group15.model.stocks.StockSectors;

/**
 * The type News item for news events where stocks gets affected.
 */
public class NewsItem {

  private final StockSectors sector;
  private BigDecimal changePercent;
  private final String headline;
  private final BigDecimal volatility;
  private int durationUpdates;
  private final Instant when;
  private boolean appliedChange;

  /**
   * Instantiates a new News item.
   *
   * @param sector          the sector
   * @param changePercent   the change percent
   * @param headline        the headline
   * @param volatility      the volatility
   * @param durationUpdates the duration updates
   * @param when            the when
   * @param appliedChange   the applied change
   */
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

  /**
   * Reduce duration of the news item, when at 0 the {@link NewsArchive} removes
   * it from the active list.
   */
  public void reduceDuration() {
    if (durationUpdates > 0) {
      durationUpdates--;
    }
  }

  /**
   * When the duration is 0 or under the news is expired.
   *
   *@return {@code true} if news duration updates are 0 or for some reason negative,
   *{@code false} otherwise
   */
  public boolean isExpired() {
    return durationUpdates <= 0;
  }

  /**
   * Footer text string which displays the duration of the news in a sentence.
   *
   * @return the string containing the duration updates and description
   */
  public String footerText() {
    if (durationUpdates <= 0) {
      return null;
    }
    return "Volatility increased for " + durationUpdates + " updates";
  }

  /**
   * For creating an information dialog using the news dialog,
   * does not affect stocks but shows information.
   *
   * @param headline the headline
   * @param message  the message
   * @return the news item
   */
  public static NewsItem info(String headline, String message) {
    return new NewsItem(null, null,
        headline, null, 0, Instant.now(), false);
  }

  /**
   * Sets applied change, the applied change is true if the news
   * change percent has been applied to the affected stocks.
   *
   * @param appliedChange the applied change
   */
  public void setAppliedChange(boolean appliedChange) {
    this.appliedChange = appliedChange;
  }

  /**
   * Returns the stock sector the news affects.
   *
   * @return the stock sector
   */
  public StockSectors sector() {
    return sector;
  }

  /**
   * Returns the initial change percent of the stock price.
   *
   * @return the change percent
   */
  public BigDecimal changePercent() {
    return changePercent;
  }

  /**
   * Returns the headline for the news.
   *
   * @return the string containing the headline text
   */
  public String headline() {
    return headline;
  }

  /**
   * Returns the volatility multiplier for the news.
   *
   * @return the volatility multiplier
   */
  public BigDecimal volatility() {
    return volatility;
  }

  /**
   * Returns the amount of updates the news will last.
   *
   * @return the int duration updates
   */
  public int durationUpdates() {
    return durationUpdates;
  }

  /**
   * Returns the time of the news creation.
   *
   * @return the instant
   */
  public Instant when() {
    return when;
  }

  /**
   * Returns a true if the news has applied initial change to the stocks,
   * and false otherwise.
   *
   * @return {@code true} if the change is applied, {@code false} otherwise
   */
  public boolean appliedChange() {
    return appliedChange;
  }
}