package ntnu.idatt2003.group15.utilities.newsparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import ntnu.idatt2003.group15.model.exceptions.UnsupportedFileTypeException;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.Test;

class NewsLoaderTest {

  private static final String CSV_FIXTURE = "src/test/resources/news/stock_news.csv";

  @Test
  void loadParsesEveryCsvRow() {
    List<NewsItem> items = new NewsLoader(CSV_FIXTURE).load();

    assertEquals(2, items.size());
  }

  @Test
  void loadMapsCsvFieldsInOrder() {
    NewsItem first = new NewsLoader(CSV_FIXTURE).load().getFirst();

    assertEquals("Quantum breakthrough lifts whole tech sector", first.headline());
    assertEquals(StockSectors.TECHNOLOGY, first.sector());
    assertEquals(0, BigDecimal.valueOf(1.15).compareTo(first.volatility()));
    assertEquals(0, BigDecimal.valueOf(0.12).compareTo(first.changePercent()));
    assertEquals(3, first.durationUpdates());
  }

  @Test
  void unsupportedExtensionThrowsAtConstruction() {
    assertThrows(UnsupportedFileTypeException.class, () -> new NewsLoader("news.xml"));
  }

  @Test
  void missingCsvFileThrowsFileReaderException() {
    NewsLoader loader = new NewsLoader("does/not/exist.csv");

    assertThrows(FileReaderException.class, loader::load);
  }
}
