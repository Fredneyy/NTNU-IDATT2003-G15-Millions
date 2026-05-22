package ntnu.idatt2003.group15.utilities.StockParser;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.StockSectors;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import ntnu.idatt2003.group15.utilities.JsonParser;

/**
 * {@link StockParser} for JSON files containing a top-level array of stock
 * objects with the keys {@code symbol}, {@code company}, {@code salesPrice},
 * {@code drift}, {@code volatility} and {@code sectors} (array of
 * {@link StockSectors} labels).
 */
public class JsonStockParser implements StockParser {

  @Override
  public List<Stock> parse(String filePath) {
    Object parsed = readJson(filePath);
    if (!(parsed instanceof List<?> entries)) {
      throw new FileReaderException(
          "Stock JSON file must contain a top-level array: " + filePath, null);
    }
    List<Stock> stocks = new ArrayList<>();
    for (Object entry : entries) {
      if (!(entry instanceof Map<?, ?> raw)) {
        throw new FileReaderException(
            "Stock JSON entries must be objects: " + filePath, null);
      }
      List<StockSectors> sectors = new ArrayList<>();
      if (raw.get("sectors") instanceof List<?> sectorLabels) {
        for (Object label : sectorLabels) {
          sectors.add(StockSectors.fromLabel(String.valueOf(label)));
        }
      }
      stocks.add(new Stock(
          String.valueOf(raw.get("symbol")),
          String.valueOf(raw.get("company")),
          asDecimal(raw.get("salesPrice")),
          asDouble(raw.get("drift")),
          asDouble(raw.get("volatility")),
          sectors));
    }
    return stocks;
  }

  private static Object readJson(String filePath) {
    try {
      String text = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
      return JsonParser.parse(text);
    } catch (Exception e) {
      throw new FileReaderException("Error reading file " + filePath, e);
    }
  }

  private static BigDecimal asDecimal(Object value) {
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    return new BigDecimal(String.valueOf(value));
  }

  private static double asDouble(Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return Double.parseDouble(String.valueOf(value));
  }
}
