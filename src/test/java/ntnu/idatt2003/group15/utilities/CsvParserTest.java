package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import org.junit.jupiter.api.Test;

class CsvParserTest {

  private static final String QUOTED_FIXTURE = "src/test/resources/csv/quoted.csv";

  @Test
  void parseReadsEveryLineIncludingHeader() {
    List<List<String>> rows = new CsvParser().parse(QUOTED_FIXTURE);
    assertEquals(3, rows.size());
  }

  @Test
  void parseSplitsHeaderRowIntoThreeCells() {
    List<List<String>> rows = new CsvParser().parse(QUOTED_FIXTURE);
    assertEquals(List.of("name", "note", "count"), rows.getFirst());
  }

  @Test
  void parseKeepsQuotedCommaInsideASingleCell() {
    List<List<String>> rows = new CsvParser().parse(QUOTED_FIXTURE);
    List<String> aliceRow = rows.get(1);
    assertEquals(3, aliceRow.size());
    assertEquals("Alice", aliceRow.get(0));
    assertEquals("hello, world", aliceRow.get(1));
    assertEquals("3", aliceRow.get(2));
  }

  @Test
  void parseHandlesPlainRowWithoutQuotes() {
    List<List<String>> rows = new CsvParser().parse(QUOTED_FIXTURE);
    assertEquals(List.of("Bob", "plain note", "1"), rows.get(2));
  }

  @Test
  void missingFileThrowsFileReaderException() {
    assertThrows(FileReaderException.class, () -> new CsvParser().parse("does/not/exist.csv"));
  }
}
