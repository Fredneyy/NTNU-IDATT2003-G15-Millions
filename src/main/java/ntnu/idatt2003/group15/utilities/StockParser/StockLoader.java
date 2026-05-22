package ntnu.idatt2003.group15.utilities.StockParser;

import java.util.List;
import ntnu.idatt2003.group15.model.Stock;

/**
 * Loads {@link Stock} instances from a file. The format is selected from the
 * file extension and delegated to a matching {@link StockParser}.
 *
 * <p>Adding a new format means adding a new {@link StockParser} implementation
 * and one branch in {@link #selectParser(String)} — no caller of this class
 * needs to change.
 */
public class StockLoader {

  private final String filePath;
  private final StockParser parser;

  public StockLoader(String filePath) {
    this.filePath = filePath;
    this.parser = selectParser(filePath);
  }

  /**
   * Reads the configured file and returns the stocks it contains.
   *
   * @return the parsed stocks
   */
  public List<Stock> load() {
    return parser.parse(filePath);
  }

  private static StockParser selectParser(String filePath) {
    String lower = filePath.toLowerCase();
    if (lower.endsWith(".csv")) {
      return new CsvStockParser();
    }
    if (lower.endsWith(".json")) {
      return new JsonStockParser();
    }
    throw new IllegalArgumentException("Unsupported stock file extension: " + filePath);
  }
}
