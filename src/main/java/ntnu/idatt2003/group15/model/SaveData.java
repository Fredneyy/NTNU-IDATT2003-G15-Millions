package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Parsed game state loaded from a save file.
 *
 * @param playerName     name of the saved trader
 * @param cash           cash on hand
 * @param difficulty     persisted difficulty multiplier ({@code null} → default)
 * @param week           the simulation week to restore ({@code null} → default 1)
 * @param savedAt        when the save was written ({@code null} if missing)
 * @param shares         portfolio lots, each {@code (symbol, quantity, pricePerShare)}
 * @param stockPrices    latest sales price per stock symbol
 */
public record SaveData(
    String playerName,
    BigDecimal cash,
    Double difficulty,
    Integer week,
    Instant savedAt,
    List<ShareEntry> shares,
    Map<String, BigDecimal> stockPrices
) {
  public record ShareEntry(String symbol, BigDecimal quantity, BigDecimal pricePerShare) {}
}
