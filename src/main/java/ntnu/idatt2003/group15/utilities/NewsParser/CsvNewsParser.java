package ntnu.idatt2003.group15.utilities.NewsParser;

import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.utilities.CsvParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CsvNewsParser implements NewsParser {

  private final CsvParser csvParser = new CsvParser();

  @Override
  public List<NewsItem> parse(String filePath) {
    List<List<String>> rows = new ArrayList<>(csvParser.parse(filePath));
    rows.removeFirst();
    List<NewsItem> newsItems = new ArrayList<>();
    for (List<String> row : rows) {
      newsItems.add(new NewsItem(
          StockSectors.fromLabel(row.get(1)),
          BigDecimal.valueOf(Double.parseDouble(row.get(3))),
          row.getFirst(),
          BigDecimal.valueOf(Double.parseDouble(row.get(2))),
          Integer.parseInt(row.getLast()),
          null, false
      ));
    }
    return newsItems;
  }
}
