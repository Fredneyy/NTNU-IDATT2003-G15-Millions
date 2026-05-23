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
 * @param startingMoney  starting balance the session was opened with
 * @param difficulty     persisted difficulty multiplier ({@code null} → default)
 * @param week           the simulation week to restore ({@code null} → default 1)
 * @param savedAt        when the save was written ({@code null} if missing)
 * @param shares         portfolio lots, each {@code (symbol, quantity, pricePerShare)}
 * @param stockPrices    latest sales price per stock symbol
 * @param transactions   persisted trade ledger; {@code null}/empty if the save predates this field
 */
public record SaveData(
    String playerName,
    BigDecimal cash,
    BigDecimal startingMoney,
    Double difficulty,
    Integer week,
    Instant savedAt,
    List<ShareEntry> shares,
    Map<String, BigDecimal> stockPrices,
    List<TxEntry> transactions
) {
  /**
   * A record class representing a share entry.
   */
  public record ShareEntry(String symbol, BigDecimal quantity, BigDecimal pricePerShare) {}

  /**
   * A single persisted transaction.
   *
   * @param type              "BUY" or "SELL"
   * @param symbol            stock symbol traded
   * @param quantity          number of shares
   * @param pricePerShare     original lot price (cost basis); same as
   *{@code salePricePerShare} for buys
   * @param week              simulation week the transaction took place
   * @param committedAt       wall-clock timestamp (drives "X ago" labels)
   * @param salePricePerShare actual market price at sale time, sells only ({@code null} for buys)
   * @param proceeds          net cash received from a sale, sells only ({@code null} for buys)
   */
  public record TxEntry(
      String type,
      String symbol,
      BigDecimal quantity,
      BigDecimal pricePerShare,
      Integer week,
      Instant committedAt,
      BigDecimal salePricePerShare,
      BigDecimal proceeds
  ) {}
}
