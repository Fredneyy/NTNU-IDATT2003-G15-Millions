package ntnu.idatt2003.group15.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Duration;
import java.util.Random;

public class NewsArchive {

  private final ObservableList<NewsItem> activeNewsItems = FXCollections.observableArrayList();
  private final ObservableList<NewsItem> loadedNewsItems = FXCollections.observableArrayList();
  private static final Duration TICK_STDDEV = Duration.seconds(15);
  private static final Duration MIN_TICK = Duration.seconds(5);
  private static final double DEFAULT_INTERVAL_SECONDS = 60.0;
  private final Random random = new Random();

  public ObservableList<NewsItem> getActiveNewsItems() {
    return activeNewsItems;
  }

  public void addNewItem(NewsItem item) {
    loadedNewsItems.add(item);
  }

  public void advance() {
    for (NewsItem item : activeNewsItems) {
      item.reduceDuration();
    }
    activeNewsItems.removeIf(NewsItem::isExpired);
  }

  public void publishNews() {
    FilteredList<NewsItem> sortedNews =  new FilteredList<>(loadedNewsItems,
        item -> !activeNewsItems.contains(item));
    if (!sortedNews.isEmpty()) {
      int randomIndex = random.nextInt(0, sortedNews.size());
      activeNewsItems.add(sortedNews.get(randomIndex));
    }
  }

}
