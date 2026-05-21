package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.StockSectors;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import org.junit.jupiter.api.Test;

class StockLoaderTest {

  private static final String CSV_FIXTURE = "src/test/resources/factories/stocks.csv";
  private static final String JSON_FIXTURE = "src/test/resources/factories/stocks.json";

  @Test
  void loadParsesEveryCsvRow() {
    List<Stock> stocks = new StockLoader(CSV_FIXTURE).load();

    assertEquals(2, stocks.size());
  }

  @Test
  void loadMapsCsvFieldsAndSectors() {
    Stock first = new StockLoader(CSV_FIXTURE).load().getFirst();

    assertEquals("APLN", first.getSymbol());
    assertEquals("Applena Technologies", first.getCompany());
    assertEquals(0, first.getSalesPrice().compareTo(BigDecimal.valueOf(182.45)));
    assertEquals(0.0008, first.getDrift());
    assertEquals(0.22, first.getVolatility());
    assertEquals(List.of(StockSectors.TECHNOLOGY, StockSectors.CONSUMER), first.getCategories());
  }

  @Test
  void loadParsesEveryJsonEntry() {
    List<Stock> stocks = new StockLoader(JSON_FIXTURE).load();

    assertEquals(2, stocks.size());
  }

  @Test
  void loadMapsJsonFieldsAndSectors() {
    Stock first = new StockLoader(JSON_FIXTURE).load().getFirst();

    assertEquals("APLN", first.getSymbol());
    assertEquals("Applena Technologies", first.getCompany());
    assertEquals(0, first.getSalesPrice().compareTo(BigDecimal.valueOf(182.45)));
    assertEquals(0.0008, first.getDrift());
    assertEquals(0.22, first.getVolatility());
    assertEquals(List.of(StockSectors.TECHNOLOGY, StockSectors.CONSUMER), first.getCategories());
  }

  @Test
  void unsupportedExtensionThrowsAtConstruction() {
    assertThrows(IllegalArgumentException.class, () -> new StockLoader("stocks.xml"));
  }

  @Test
  void missingCsvFileThrowsFileReaderException() {
    StockLoader loader = new StockLoader("does/not/exist.csv");

    assertThrows(FileReaderException.class, loader::load);
  }

  @Test
  void missingJsonFileThrowsFileReaderException() {
    StockLoader loader = new StockLoader("does/not/exist.json");

    assertThrows(FileReaderException.class, loader::load);
  }
}
