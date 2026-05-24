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
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Transaction;

/**
 * Serializes the current game state (player, exchange, settings) to a JSON file
 * at a user-chosen path. Pure utility — no JavaFX, no UI.
 */
public final class SaveGameUtil {

  private SaveGameUtil() {}

  /**
   * Write the current game state to {@code file} as pretty-printed JSON.
   *
   * @throws IOException if writing fails
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

  private static String buildJson(PlayerController player,
                                  ExchangeController exchange,
                                  GameSettings settings) {
    String sb = "{\n" +
        "  \"version\": 1,\n" +
        "  \"savedAt\": " + quote(Instant.now().toString()) + ",\n" +
        "  \"settings\": " + settingsJson(settings) + ",\n" +
        "  \"player\": " + playerJson(player) + ",\n" +
        "  \"exchange\": " + exchangeJson(exchange) + "\n" +
        "}\n";
    return sb;
  }

  private static String settingsJson(GameSettings s) {
    if (s == null) return "null";
    StringJoiner j = new StringJoiner(", ", "{", "}");
    j.add(field("difficulty", number(s.getDifficulty())));
    j.add(field("volatilityMultiplier", number(s.getVolatilityMultiplier())));
    j.add(field("maxEventChance", number(s.getMaxEventChance())));
    return j.toString();
  }

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

  private static String transactionsJson(PlayerController player) {
    StringJoiner arr = new StringJoiner(", ", "[", "]");
    for (Transaction tx : player.getTransactionArchive().getTransactionsProperty()) {
      arr.add(transactionJson(tx));
    }
    return arr.toString();
  }

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

  private static String field(String key, String value) {
    return quote(key) + ": " + value;
  }

  private static String quote(String s) {
    if (s == null) return "null";
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

  private static String number(BigDecimal v) {
    return v == null ? "null" : v.toPlainString();
  }

  private static String number(double v) {
    if (Double.isNaN(v) || Double.isInfinite(v)) return "null";
    return Double.toString(v);
  }

  private static String number(int v) {
    return Integer.toString(v);
  }
}
