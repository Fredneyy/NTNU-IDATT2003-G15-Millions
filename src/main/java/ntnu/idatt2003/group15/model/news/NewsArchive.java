package ntnu.idatt2003.group15.model.news;

import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * The news archive for storing the {@link NewsItem}, and publishing for events.
 */
public class NewsArchive {

  private final ObservableList<NewsItem> activeNewsItems = FXCollections.observableArrayList();
  private final ObservableList<NewsItem> loadedNewsItems = FXCollections.observableArrayList();
  private final Random random = new Random();

  /**
   * Instantiates a new News archive.
   *
   * @param news the list of loaded news
   */
  public NewsArchive(List<NewsItem> news) {
    this.loadedNewsItems.addAll(news);
  }

  /**
   * Gets the observableList which contains the active news.
   *
   * @return the active news items
   */
  public ObservableList<NewsItem> getActiveNewsItems() {
    return activeNewsItems;
  }

  /**
   * Iterates over the active news items and reduces the duration of each {@link NewsItem}
   * also removes expired {@link NewsItem} objects.
   *
   */
  public void advance() {
    for (NewsItem item : activeNewsItems) {
      item.reduceDuration();
    }
    activeNewsItems.removeIf(NewsItem::isExpired);
  }

  /**
   * Clears the active news items list.
   */
  public void reset() {
    activeNewsItems.clear();
  }

  /**
   * Puts a random {@link NewsItem} in the active news list for observers to see.
   *
   * @param maxEventChance the chance of a new {@link NewsItem} being added to active news items
   */
  public void publishNews(double maxEventChance) {
    if (random.nextDouble() > maxEventChance) {
      return;
    }
    FilteredList<NewsItem> sortedNews =  new FilteredList<>(loadedNewsItems,
        item -> !activeNewsItems.contains(item));
    if (!sortedNews.isEmpty()) {
      int randomIndex = random.nextInt(0, sortedNews.size());
      NewsItem item = sortedNews.get(randomIndex);
      item.setAppliedChange(false);
      activeNewsItems.add(item);
    }
  }

}
