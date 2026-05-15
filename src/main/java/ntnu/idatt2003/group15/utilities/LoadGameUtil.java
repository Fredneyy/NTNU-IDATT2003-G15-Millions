package ntnu.idatt2003.group15.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.math.BigDecimal;
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

  private LoadGameUtil() {}

  public static SaveData load(File file) throws IOException {
    String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
    Object parsed = JsonParser.parse(text);
    if (!(parsed instanceof Map<?, ?> rootMap)) {
      throw new IOException("Save file is not a JSON object");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> root = (Map<String, Object>) rootMap;

    Map<String, Object> player = asMap(root.get("player"));
    Map<String, Object> exchange = asMap(root.get("exchange"));
    Map<String, Object> settings = asMap(root.get("settings"));

    String name = asString(player.get("name"));
    BigDecimal cash = asDecimal(player.get("cash"));
    Double difficulty = settings == null ? null : asDouble(settings.get("difficulty"));
    Integer week = exchange == null ? null : asInt(exchange.get("week"));
    Instant savedAt = asInstant(root.get("savedAt"));

    List<SaveData.ShareEntry> shares = new ArrayList<>();
    Object pf = player.get("portfolio");
    if (pf instanceof List<?> list) {
      for (Object item : list) {
        Map<String, Object> share = asMap(item);
        if (share == null) continue;
        String symbol = asString(share.get("symbol"));
        BigDecimal qty = asDecimal(share.get("quantity"));
        BigDecimal pps = asDecimal(share.get("pricePerShare"));
        if (symbol != null && qty != null && pps != null) {
          shares.add(new SaveData.ShareEntry(symbol, qty, pps));
        }
      }
    }

    Map<String, BigDecimal> stockPrices = new LinkedHashMap<>();
    if (exchange != null && exchange.get("stocks") instanceof List<?> stockList) {
      for (Object item : stockList) {
        Map<String, Object> stock = asMap(item);
        if (stock == null) continue;
        String symbol = asString(stock.get("symbol"));
        BigDecimal price = asDecimal(stock.get("salesPrice"));
        if (symbol != null && price != null) {
          stockPrices.put(symbol, price);
        }
      }
    }

    return new SaveData(name, cash, difficulty, week, savedAt, shares, stockPrices);
  }

  // ---- coercion helpers ----

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object v) {
    return v instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
  }

  private static String asString(Object v) {
    return v == null ? null : v.toString();
  }

  private static BigDecimal asDecimal(Object v) {
    if (v instanceof BigDecimal b) return b;
    if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
    if (v instanceof String s && !s.isBlank()) {
      try { return new BigDecimal(s); } catch (NumberFormatException ignored) { /* fall through */ }
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
    if (v == null) return null;
    try { return Instant.parse(v.toString()); }
    catch (RuntimeException e) { return null; }
  }
}
