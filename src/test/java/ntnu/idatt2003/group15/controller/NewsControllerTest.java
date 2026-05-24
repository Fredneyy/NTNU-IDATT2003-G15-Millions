package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import ntnu.idatt2003.group15.model.news.NewsArchive;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewsControllerTest {

  private NewsArchive archive;
  private NewsController controller;

  @BeforeEach
  void setUp() {
    NewsItem item = new NewsItem(
        "headline",
        StockSectors.TECHNOLOGY,
        BigDecimal.ONE,
        BigDecimal.ZERO,
        3,
        Instant.now(),
        false);
    archive = new NewsArchive(List.of(item));
    controller = new NewsController(archive);
  }

  @Test
  void getNewsObservableReturnsBackingActiveList() {
    assertSame(archive.getActiveNewsItems(), controller.getNewsObservable());
  }

  @Test
  void publishMovesItemIntoActiveList() {
    controller.publish(1);
    assertEquals(1, controller.getNewsObservable().size());
  }

  @Test
  void advanceWeekReducesDuration() {
    controller.publish(1);
    NewsItem published = controller.getNewsObservable().getFirst();
    int before = published.durationUpdates();

    controller.advanceWeek();

    assertEquals(before - 1, published.durationUpdates());
  }

  @Test
  void advanceWeekRemovesExpiredItems() {
    NewsItem shortItem = new NewsItem(
        "short", StockSectors.TECHNOLOGY,
        BigDecimal.ONE, BigDecimal.ZERO, 1, Instant.now(), false);
    NewsController shortLived = new NewsController(new NewsArchive(List.of(shortItem)));
    shortLived.publish(1);

    shortLived.advanceWeek();

    assertTrue(shortLived.getNewsObservable().isEmpty());
  }

  @Test
  void constructorRejectsNullArchive() {
    assertThrows(NullPointerException.class, () -> new NewsController(null));
  }
}
