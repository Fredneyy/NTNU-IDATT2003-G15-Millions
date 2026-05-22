package ntnu.idatt2003.group15.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Random;

public class NewsArchive {

  private final ObservableList<NewsItem> activeNewsItems = FXCollections.observableArrayList();
  private final ObservableList<NewsItem> loadedNewsItems = FXCollections.observableArrayList();
  private final Random random = new Random();

  public NewsArchive(List<NewsItem> news) {
    this.loadedNewsItems.addAll(news);
  }

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
      NewsItem item = sortedNews.get(randomIndex);
      item.setAppliedChange(false);
      activeNewsItems.add(item);
      System.out.println(sortedNews.get(randomIndex).toString());
    }
  }

}
