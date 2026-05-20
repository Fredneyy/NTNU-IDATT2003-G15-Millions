package ntnu.idatt2003.group15.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class NewsArchive {

  private final ObservableList<NewsItem> newsItems = FXCollections.observableArrayList();

  public List<NewsItem> getNewsFromSector(StockSectors sector) {
    return newsItems.stream().filter(item -> item.sector().equals(sector)).toList();
  }

  public ObservableList<NewsItem> getNewsItems() {
    return newsItems;
  }

  public void addItem(NewsItem item) {
    newsItems.add(item);
  }

  public void advance() {
    for (NewsItem item : newsItems) {
      int durationUpdates = item.durationUpdates();
      durationUpdates -= 1;
      if (durationUpdates == 0) {
        newsItems.remove(item);
      }
    }
  }

}
