package ntnu.idatt2003.group15.utilities;

import ntnu.idatt2003.group15.model.exceptions.FileReaderException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides utility methods for parsing and writing CSV data.
 */
public class CsvUtil {

  /**
   * Read csv file list.
   *
   * @param filePath the file path
   * @return {@code List} containing every comma seperated string.
   * @throws FileReaderException if reader runs into a problem during operation
   */
  public List<String> readCsvFile(String filePath) throws FileReaderException {
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      return lines.map(line -> Arrays.asList(line.split(","))).flatMap(Collection::stream).toList();
    } catch (Exception e) {
      throw new FileReaderException("Error reading file " + filePath, e);
    }
  }

}
