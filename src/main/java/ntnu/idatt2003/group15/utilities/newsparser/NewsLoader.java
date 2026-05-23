package ntnu.idatt2003.group15.utilities.newsparser;

import java.util.List;
import ntnu.idatt2003.group15.model.exceptions.FileReaderException;
import ntnu.idatt2003.group15.model.exceptions.UnsupportedFileTypeException;
import ntnu.idatt2003.group15.model.news.NewsItem;

/**
 * The type News loader.
 *
 * <p>The newsloader gets a filepath and chooses a parser based on the file format.
 * The {@code load()} function uses the parsers to return a List of {@link NewsItem}</p>
 */
public class NewsLoader {

  private final String filePath;
  private final NewsParser parser;

  /**
   * Instantiates a new News loader.
   *
   * @param filePath the file path to read from
   * @throws UnsupportedFileTypeException if the loader could not support file type
   * @throws FileReaderException if csv parser could not read the file
   */
  public NewsLoader(String filePath) throws UnsupportedFileTypeException {
    this.filePath = filePath;
    this.parser = selectParser(filePath);
  }

  /**
   * Load list.
   *
   * @return the list of loaded {@link NewsItem}
   *
   */
  public List<NewsItem> load() {
    return parser.parse(filePath);
  }

  private static NewsParser selectParser(String filePath) {
    String lower = filePath.toLowerCase();
    if (lower.endsWith(".csv")) {
      return new CsvNewsParser();
    }
    throw new UnsupportedFileTypeException("Unsupported file type" + filePath);
  }
}
