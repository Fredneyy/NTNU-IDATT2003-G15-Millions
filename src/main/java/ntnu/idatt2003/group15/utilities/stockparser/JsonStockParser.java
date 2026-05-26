package ntnu.idatt2003.group15.utilities.stockparser;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.utilities.JsonParser;

/**
 * {@link StockParser} for JSON files containing a top-level array of stock
 * objects with the keys {@code symbol}, {@code company}, {@code salesPrice},
 * {@code drift}, {@code volatility} and {@code sectors} (array of
 * {@link StockSectors} labels).
 */
public class JsonStockParser implements StockParser {

  @Override
  public List<Stock> parse(String filePath) throws FileReaderException {
    Object parsed = readJson(filePath);
    if (!(parsed instanceof List<?> entries)) {
      throw new FileReaderException(
          "The stock file '" + filePath
              + "' is not in the expected format. The JSON has to start with an array '[' "
              + "of stock objects.");
    }
    List<Stock> stocks = new ArrayList<>();
    for (Object entry : entries) {
      if (!(entry instanceof Map<?, ?> raw)) {
        throw new FileReaderException(
            "The stock file '" + filePath
                + "' contains a malformed entry. Each item in the array has to be a stock object "
                + "with 'symbol', 'company', 'salesPrice', 'drift', 'volatility' and 'sectors'.");
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
      throw new FileReaderException(
          "Couldn't read the stock file '" + filePath
              + "'. Make sure the file exists and that it's valid JSON.");
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
