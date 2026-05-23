package ntnu.idatt2003.group15.model.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NewsArchiveTest {

  @Nested
  @DisplayName("Positive NewsArchive Tests")
  class positiveNewsArchiveTests {

    private NewsItem itemA;
    private NewsItem itemB;
    private NewsArchive archive;

    @BeforeEach
    void setUp() {
      itemA = newItem("A", 2);
      itemB = newItem("B", 1);
      archive = new NewsArchive(List.of(itemA, itemB));
    }

    @Test
    void newArchiveHasNoActiveItems() {
      assertTrue(archive.getActiveNewsItems().isEmpty());
    }

    @Test
    void publishNewsMovesAnItemIntoActive() {
      archive.publishNews();
      assertEquals(1, archive.getActiveNewsItems().size());
    }

    @Test
    void publishNewsDoesNotRepublishAlreadyActiveItems() {
      archive.publishNews();
      archive.publishNews();
      ObservableList<NewsItem> active = archive.getActiveNewsItems();
      assertEquals(2, active.size());
      assertEquals(2, active.stream().distinct().count());
    }

    @Test
    void publishNewsBeyondLoadedPoolIsNoOp() {
      archive.publishNews();
      archive.publishNews();
      archive.publishNews();
      assertEquals(2, archive.getActiveNewsItems().size());
    }

    @Test
    void publishNewsClearsAppliedChangeFlag() {
      itemA.setAppliedChange(true);
      itemB.setAppliedChange(true);
      archive.publishNews();
      NewsItem published = archive.getActiveNewsItems().getFirst();
      assertFalse(published.appliedChange());
    }

    @Test
    void advanceReducesDurationOfActiveItems() {
      // Single-item archive keeps the assertion deterministic — random publish
      // order doesn't matter and no item gets evicted mid-test.
      NewsItem longLived = newItem("long", 5);
      NewsArchive only = new NewsArchive(List.of(longLived));
      only.publishNews();

      only.advance();

      assertEquals(4, only.getActiveNewsItems().getFirst().durationUpdates());
    }

    @Test
    void advanceRemovesExpiredItems() {
      NewsItem shortItem = newItem("short", 1);
      NewsArchive only = new NewsArchive(List.of(shortItem));
      only.publishNews();
      assertEquals(1, only.getActiveNewsItems().size());

      only.advance();

      assertTrue(only.getActiveNewsItems().isEmpty());
    }

    @Test
    void publishNewsOnEmptyArchiveAddsNothing() {
      NewsArchive empty = new NewsArchive(List.of());
      empty.publishNews();
      assertTrue(empty.getActiveNewsItems().isEmpty());
    }
  }

  private static NewsItem newItem(String headline, int duration) {
    return new NewsItem(
        headline,
        StockSectors.TECHNOLOGY,
        BigDecimal.ONE,
        BigDecimal.ZERO,
        duration,
        Instant.now(),
        false);
  }
}
