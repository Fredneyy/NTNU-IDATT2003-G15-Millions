package ntnu.idatt2003.group15.utilities.newsparser;

import java.util.List;
import ntnu.idatt2003.group15.model.news.NewsItem;

/**
 * Reads a file in some format and returns the {@link NewsItem} instances it describes.
 *
 * <p>Implementations are owned by {@link NewsLoader}, which selects the right on for
 * the given file. Adding a new file format means adding another implementation that
 * can read that file type.</p>
 */
public interface NewsParser {
  /**
   * Parse the file at the file path into the stocks.
   *
   * @param filePath the file path
   * @return the parsed {@link NewsItem}
   */
  List<NewsItem> parse(String filePath);
}
