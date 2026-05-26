package ntnu.idatt2003.group15.utilities;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ntnu.idatt2003.group15.model.SaveData;

/**
 * Reads a save file produced by {@link SaveGameUtil} into a {@link SaveData}.
 *
 * <p>Tolerant of missing fields — the controller layer can fall back to its
 * own defaults for anything that's {@code null}.
 */
public final class LoadGameUtil {

  /**
   * Load save data.
   *
   * @param file the file
   * @return the save data
   * @throws IOException if loading fails
   */
  public static SaveData load(File file) throws IOException {
    String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
    Object parsed = JsonParser.parse(text);
    if (!(parsed instanceof Map<?, ?> rootMap)) {
      throw new IOException(
          "The save file '" + file.getName()
              + "' is not a valid game save. It looks like the file has been edited or corrupted.");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> root = (Map<String, Object>) rootMap;

    Map<String, Object> player = asMap(root.get("player"));
    Map<String, Object> exchange = asMap(root.get("exchange"));

    List<SaveData.ShareEntry> shares = new ArrayList<>();
    Object pf = player.get("portfolio");
    if (pf instanceof List<?> list) {
      for (Object item : list) {
        Map<String, Object> share = asMap(item);
        if (share == null) {
          continue;
        }
        String symbol = asString(share.get("symbol"));
        BigDecimal qty = asDecimal(share.get("quantity"));
        BigDecimal pps = asDecimal(share.get("pricePerShare"));
        if (symbol != null && qty != null && pps != null) {
          shares.add(new SaveData.ShareEntry(symbol, qty, pps));
        }
      }
    }

    Map<String, BigDecimal> stockPrices = new LinkedHashMap<>();
    Map<String, List<BigDecimal>> stockHistories = new LinkedHashMap<>();
    if (exchange != null && exchange.get("stocks") instanceof List<?> stockList) {
      for (Object item : stockList) {
        Map<String, Object> stock = asMap(item);
        if (stock == null) {
          continue;
        }
        String symbol = asString(stock.get("symbol"));
        BigDecimal price = asDecimal(stock.get("salesPrice"));
        if (symbol != null && price != null) {
          stockPrices.put(symbol, price);
        }
        if (symbol != null && stock.get("history") instanceof List<?> rawHistory) {
          List<BigDecimal> history = new ArrayList<>(rawHistory.size());
          for (Object raw : rawHistory) {
            BigDecimal hp = asDecimal(raw);
            if (hp != null && hp.signum() > 0) {
              history.add(hp);
            }
          }
          if (!history.isEmpty()) {
            stockHistories.put(symbol, history);
          }
        }
      }
    }

    List<SaveData.TxEntry> transactions = new ArrayList<>();
    Object txList = player.get("transactions");
    if (txList instanceof List<?> list) {
      for (Object item : list) {
        Map<String, Object> tx = asMap(item);
        if (tx == null) {
          continue;
        }
        String type = asString(tx.get("type"));
        String symbol = asString(tx.get("symbol"));
        BigDecimal qty = asDecimal(tx.get("quantity"));
        BigDecimal pps = asDecimal(tx.get("pricePerShare"));
        if (type == null || symbol == null || qty == null || pps == null) {
          continue;
        }
        Integer txWeek = asInt(tx.get("week"));
        Instant committedAt = asInstant(tx.get("committedAt"));
        BigDecimal salePrice = asDecimal(tx.get("salePricePerShare"));
        BigDecimal proceeds = asDecimal(tx.get("proceeds"));
        transactions.add(new SaveData.TxEntry(
            type, symbol, qty, pps, txWeek, committedAt, salePrice, proceeds));
      }
    }

    String name = asString(player.get("name"));
    BigDecimal cash = asDecimal(player.get("cash"));
    BigDecimal startingMoney = asDecimal(player.get("startingMoney"));
    Map<String, Object> settings = asMap(root.get("settings"));
    Double difficulty = settings == null ? null : asDouble(settings.get("difficulty"));
    Integer week = exchange == null ? null : asInt(exchange.get("week"));
    Instant savedAt = asInstant(root.get("savedAt"));

    return new SaveData(name, cash, startingMoney, difficulty, week,
        savedAt, shares, stockPrices, stockHistories, transactions);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object v) {
    return v instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
  }

  private static String asString(Object v) {
    return v == null ? null : v.toString();
  }

  private static BigDecimal asDecimal(Object v) {
    if (v instanceof BigDecimal b) {
      return b;
    }
    if (v instanceof Number n) {
      return BigDecimal.valueOf(n.doubleValue());
    }
    if (v instanceof String s && !s.isBlank()) {
      try {
        return new BigDecimal(s);
      } catch (NumberFormatException ignored) {

      }
    }
    return null;
  }

  private static Double asDouble(Object v) {
    BigDecimal d = asDecimal(v);
    return d == null ? null : d.doubleValue();
  }

  private static Integer asInt(Object v) {
    BigDecimal d = asDecimal(v);
    return d == null ? null : d.intValue();
  }

  private static Instant asInstant(Object v) {
    if (v == null) {
      return null;
    }
    try {
      return Instant.parse(v.toString());
    } catch (RuntimeException e) {
      return null;
    }
  }

  private LoadGameUtil() {}
}
