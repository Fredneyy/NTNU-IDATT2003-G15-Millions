package ntnu.idatt2003.group15.utilities.StockParser;

import java.util.List;
import ntnu.idatt2003.group15.model.Stock;

/**
 * Reads a file in some format and returns the {@link Stock} instances it describes.
 *
 * <p>Implementations are owned by {@link StockLoader}, which selects the right
 * one for a given file. Adding a new file format means adding a new implementation
 * and one branch in {@link StockLoader}; no other code needs to change.
 */
public interface StockParser {

  /**
   * Parses the file at {@code filePath} into stocks.
   *
   * @param filePath path to a file in this parser's format
   * @return the parsed stocks
   */
  List<Stock> parse(String filePath);
}
