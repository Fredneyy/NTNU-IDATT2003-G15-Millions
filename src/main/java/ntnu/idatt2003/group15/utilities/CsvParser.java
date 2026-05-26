package ntnu.idatt2003.group15.utilities;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;

/**
 * Provides utility methods for parsing and writing CSV data.
 */
public class CsvParser {

  /**
   * Read a CSV file as rows and columns.
   *
   * @param filePath the file path
   * @return a list of rows, where each row is a list of trimmed, unquoted cells
   * @throws FileReaderException if the reader runs into a problem during operation
   */
  public List<List<String>> parse(String filePath) throws FileReaderException {
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      return lines
          .map(line -> Arrays.stream(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
              .map(cell -> cell.trim().replaceAll("^\"|\"$", ""))
              .toList())
          .toList();
    } catch (Exception e) {
      throw new FileReaderException(
          "Couldn't read the CSV file '" + filePath
              + "'. Make sure the file exists and that it's a valid CSV.");
    }
  }
}