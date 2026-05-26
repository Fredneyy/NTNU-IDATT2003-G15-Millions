package ntnu.idatt2003.group15.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.StringJoiner;
import ntnu.idatt2003.group15.controller.ExchangeController;
import ntnu.idatt2003.group15.controller.PlayerController;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.transactions.Transaction;

/**
 * Serializes the current game state (player, exchange, settings) to a JSON file
 * at a user-chosen path. Pure utility — no JavaFX, no UI.
 */
public final class SaveGameUtil {

  /**
   * Writes the current game state to {@code file} as pretty-printed JSON and
   * records the save in the recent-saves index.
   *
   * @param file the file to write the JSON to (overwritten if it exists)
   * @param player the player controller whose name, cash, and portfolio are saved
   * @param exchange the exchange whose week and stock history are saved
   * @param settings the game settings (difficulty, etc.) persisted with the save
   * @throws IOException if the file cannot be written
   */
  public static void save(File file,
                          PlayerController player,
                          ExchangeController exchange,
                          GameSettings settings) throws IOException {
    String json = buildJson(player, exchange, settings);
    try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      w.write(json);
    }
    SaveIndex.record(new SaveIndex.Entry(
        file.getAbsolutePath(),
        player.getName(),
        player.getMoney(),
        Instant.now()
    ));
  }

  /**
   * Builds the full save-file JSON document by stitching together the version
   * header, timestamp, and the per-section blocks for settings, player, and
   * exchange.
   *
   * @param player   the player controller serialized into the {@code player} block
   * @param exchange the exchange controller serialized into the {@code exchange} block
   * @param settings the game settings serialized into the {@code settings} block
   * @return the complete save-file JSON as a single string
   */
  private static String buildJson(PlayerController player,
                                  ExchangeController exchange,
                                  GameSettings settings) {
    String sb = "{\n"
        + "  \"version\": 1,\n"
        + "  \"savedAt\": " + quote(Instant.now().toString()) + ",\n"
        + "  \"settings\": " + settingsJson(settings) + ",\n"
        + "  \"player\": " + playerJson(player) + ",\n"
        + "  \"exchange\": " + exchangeJson(exchange) + "\n"
        + "}\n";
    return sb;
  }

  /**
   * Serializes the game settings (difficulty, volatility multiplier, and max
   * event chance) into a JSON object literal.
   *
   * @param settings the settings to serialize, or {@code null}
   * @return a JSON object string, or the literal {@code "null"} if {@code settings} is null
   */
  private static String settingsJson(GameSettings settings) {
    if (settings == null) {
      return "null";
    }
    StringJoiner j = new StringJoiner(", ", "{", "}");
    j.add(field("difficulty", number(settings.getDifficulty())));
    j.add(field("volatilityMultiplier", number(settings.getVolatilityMultiplier())));
    j.add(field("maxEventChance", number(settings.getMaxEventChance())));
    return j.toString();
  }

  /**
   * Serializes the player into a JSON object with name, cash, starting money,
   * net worth, portfolio holdings, and transaction history.
   *
   * @param player the player controller to serialize
   * @return a JSON object string representing the player's state
   */
  private static String playerJson(PlayerController player) {
    StringJoiner j = new StringJoiner(", ", "{", "}");
    j.add(field("name", quote(player.getName())));
    j.add(field("cash", number(player.getMoney())));
    j.add(field("startingMoney", number(player.getStartingMoney())));
    j.add(field("netWorth", number(player.getNetWorth())));
    j.add(field("portfolio", portfolioJson(player.getPortfolio())));
    j.add(field("transactions", transactionsJson(player)));
    return j.toString();
  }

  /**
   * Serializes the player's transaction archive into a JSON array, with each
   * entry produced by {@link #transactionJson(Transaction)}.
   *
   * @param player the player controller whose transactions are serialized
   * @return a JSON array string of every recorded transaction
   */
  private static String transactionsJson(PlayerController player) {
    StringJoiner arr = new StringJoiner(", ", "[", "]");
    for (Transaction tx : player.getTransactionArchive().getTransactionsProperty()) {
      arr.add(transactionJson(tx));
    }
    return arr.toString();
  }

  /**
   * Serializes a single transaction into a JSON object. Sales include the
   * actual sale price and net proceeds; buys omit those fields. Unknown
   * transaction subtypes are tagged with their class name for forward
   * compatibility rather than rejected.
   *
   * @param tx the transaction to serialize
   * @return a JSON object string describing the transaction
   */
  private static String transactionJson(Transaction tx) {
    StringJoiner j = new StringJoiner(", ", "{", "}");
    boolean sell = tx instanceof Sale;
    j.add(field("type", quote(sell ? "SELL" : "BUY")));
    j.add(field("symbol", quote(tx.getShare().stock().getSymbol())));
    j.add(field("quantity", number(tx.getShare().quantity())));
    j.add(field("pricePerShare", number(tx.getShare().pricePerShare())));
    j.add(field("week", number(tx.getWeek())));
    j.add(field("committedAt",
        tx.getCommittedAt() == null ? "null" : quote(tx.getCommittedAt().toString())));
    if (sell) {
      Sale s = (Sale) tx;
      j.add(field("salePricePerShare", number(s.getSalePricePerShare())));
      j.add(field("proceeds", number(s.getProceeds())));
    } else if (!(tx instanceof Purchase)) {
      // forward-compat: record unknown subtypes instead of failing
      j.add(field("type", quote(tx.getClass().getSimpleName())));
    }
    return j.toString();
  }

  /**
   * Serializes the player's portfolio into a JSON array of share lots, each
   * with the stock symbol, quantity held, and cost basis per share.
   *
   * @param portfolio the portfolio to serialize
   * @return a JSON array string of every held share lot
   */
  private static String portfolioJson(Portfolio portfolio) {
    List<Share> shares = portfolio.getShares();
    StringJoiner arr = new StringJoiner(", ", "[", "]");
    for (Share share : shares) {
      StringJoiner s = new StringJoiner(", ", "{", "}");
      s.add(field("symbol", quote(share.stock().getSymbol())));
      s.add(field("quantity", number(share.quantity())));
      s.add(field("pricePerShare", number(share.pricePerShare())));
      arr.add(s.toString());
    }
    return arr.toString();
  }

  /**
   * Serializes the exchange into a JSON object with its name, the current
   * simulation week, and an array of every listed stock.
   *
   * @param exchange the exchange controller to serialize
   * @return a JSON object string representing the exchange's state
   */
  private static String exchangeJson(ExchangeController exchange) {
    StringJoiner j = new StringJoiner(", ", "{", "}");
    j.add(field("name", quote(exchange.getExchangeName())));
    j.add(field("week", number(exchange.getWeek().get())));
    StringJoiner stocks = new StringJoiner(", ", "[", "]");
    for (Stock stock : exchange.getAllStocks()) {
      stocks.add(stockJson(stock));
    }
    j.add(field("stocks", stocks.toString()));
    return j.toString();
  }

  /**
   * Serializes a single stock into a JSON object with its symbol, company,
   * current sales price, simulator parameters (drift and volatility), the list
   * of sectors it belongs to, and its full historical price series.
   *
   * @param stock the stock to serialize
   * @return a JSON object string describing the stock
   */
  private static String stockJson(Stock stock) {
    StringJoiner j = new StringJoiner(", ", "{", "}");
    j.add(field("symbol", quote(stock.getSymbol())));
    j.add(field("company", quote(stock.getCompany())));
    j.add(field("salesPrice", number(stock.getSalesPrice())));
    j.add(field("drift", number(stock.getDrift())));
    j.add(field("volatility", number(stock.getVolatility())));
    StringJoiner cats = new StringJoiner(", ", "[", "]");
    for (StockSectors sec : stock.getCategories()) {
      cats.add(quote(sec.getLabel()));
    }
    j.add(field("sectors", cats.toString()));
    StringJoiner hist = new StringJoiner(", ", "[", "]");
    for (BigDecimal p : stock.getHistoricalPrices()) {
      hist.add(number(p));
    }
    j.add(field("history", hist.toString()));
    return j.toString();
  }

  /**
   * Formats a single JSON key-value pair as {@code "key": value}. The key is
   * quoted; the value is inserted verbatim, so callers are responsible for
   * quoting strings or formatting numbers themselves.
   *
   * @param key   the field name (will be quoted)
   * @param value the already-formatted JSON value (string, number, object, etc.)
   * @return the formatted {@code "key": value} fragment
   */
  private static String field(String key, String value) {
    return quote(key) + ": " + value;
  }

  /**
   * Wraps a string in double quotes and escapes characters that would otherwise
   * break the JSON document: backslashes, quotes, newlines, carriage returns,
   * tabs, and any other control characters (which are emitted as {@code \\uXXXX}
   * escapes).
   *
   * @param s the string to quote, or {@code null}
   * @return the quoted, escaped JSON string literal, or {@code "null"} if {@code s} is null
   */
  private static String quote(String s) {
    if (s == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\', '"' -> sb.append('\\').append(c);
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }

  /**
   * Formats a {@link BigDecimal} as a plain JSON number without scientific
   * notation, so the saved value reads exactly as the in-memory value.
   *
   * @param v the value to format, or {@code null}
   * @return the plain-string number, or {@code "null"} if {@code v} is null
   */
  private static String number(BigDecimal v) {
    return v == null ? "null" : v.toPlainString();
  }

  /**
   * Formats a {@code double} as a JSON number. NaN and infinity have no JSON
   * representation, so they are written as {@code "null"} rather than producing
   * invalid output.
   *
   * @param v the value to format
   * @return the number as a string, or {@code "null"} if NaN or infinite
   */
  private static String number(double v) {
    if (Double.isNaN(v) || Double.isInfinite(v)) {
      return "null";
    }
    return Double.toString(v);
  }

  /**
   * Formats an {@code int} as a JSON number.
   *
   * @param v the value to format
   * @return the number as a string
   */
  private static String number(int v) {
    return Integer.toString(v);
  }

  private SaveGameUtil() {}
}
