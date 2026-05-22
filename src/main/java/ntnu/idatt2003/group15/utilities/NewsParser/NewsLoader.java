package ntnu.idatt2003.group15.utilities.NewsParser;

import ntnu.idatt2003.group15.model.NewsItem;

import java.util.List;

public class NewsLoader {

  private final String filePath;
  private final NewsParser parser;

  public NewsLoader(String filePath) {
    this.filePath = filePath;
    this.parser = selectParser(filePath);
  }

  public List<NewsItem> load() {
    return parser.parse(filePath);
  }

  private static NewsParser selectParser(String filePath) {
    String lower = filePath.toLowerCase();
    if (lower.endsWith(".csv")) {
      return new CsvNewsParser();
    }
    throw new IllegalArgumentException("Unsupported file type" + filePath);
  }
}
