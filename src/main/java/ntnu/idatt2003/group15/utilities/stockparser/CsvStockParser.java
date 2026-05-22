package ntnu.idatt2003.group15.utilities.stockparser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.utilities.CsvParser;

/**
 * {@link StockParser} for CSV files with the columns
 * {@code symbol,company,salesPrice,drift,volatility,sectors}, where {@code sectors}
 * is a {@code |}-separated list of {@link StockSectors} labels. The first row is
 * treated as a header and skipped.
 */
public class CsvStockParser implements StockParser {

  private final CsvParser csvParser = new CsvParser();

  @Override
  public List<Stock> parse(String filePath) throws FileReaderException {
    List<List<String>> rows = new ArrayList<>(csvParser.parse(filePath));
    rows.removeFirst();
    List<Stock> stocks = new ArrayList<>();
    for (List<String> row : rows) {
      List<StockSectors> sectors = new ArrayList<>();
      for (String sector : row.getLast().split("\\|")) {
        sectors.add(StockSectors.fromLabel(sector));
      }
      stocks.add(new Stock(
          row.getFirst(),
          row.get(1),
          BigDecimal.valueOf(Double.parseDouble(row.get(2))),
          Double.parseDouble(row.get(3)),
          Double.parseDouble(row.get(4)),
          sectors));
    }
    return stocks;
  }
}
