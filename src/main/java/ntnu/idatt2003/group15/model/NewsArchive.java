package ntnu.idatt2003.group15.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

public class NewsArchive {

  private final ObservableList<NewsItem> activeNewsItems = FXCollections.observableArrayList();
  private final ObservableList<NewsItem> loadedNewsItems = FXCollections.observableArrayList();
  private static final Duration TICK_STDDEV = Duration.seconds(15);
  private static final Duration MIN_TICK = Duration.seconds(5);
  private static final double DEFAULT_INTERVAL_SECONDS = 60.0;
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
      
      // Add slight gaussian randomization to the changePercent
      if (item.changePercent() != null) {
          double stdDev = 0.05; // 5% standard deviation around the base rate
          double baseChange = item.changePercent().doubleValue() - 1.0; 
          double randomAdjustment = random.nextGaussian() * stdDev; 
          
          double newChange = baseChange + randomAdjustment;
          // E.g. base=0.10 (+10%), adjustment=-0.03 -> newChange=0.07 (+7%)
          // Which becomes 1.07 as change percent multiplier
          // Or base=-0.10 (-10%), adjustment=+0.02 -> newChange=-0.08 (-8%) -> 0.92
          
          // Make sure the news item's original polarity remains (e.g. positive news stays positive)
          if (baseChange > 0 && newChange <= 0) {
              newChange = 0.01; // Force minimum positive change
          } else if (baseChange < 0 && newChange >= 0) {
              newChange = -0.01; // Force minimum negative change
          }
          
          item.setChangePercent(BigDecimal.valueOf(1.0 + newChange));
      }
      
      item.setAppliedChange(false);
      activeNewsItems.add(item);
    }
  }

}
