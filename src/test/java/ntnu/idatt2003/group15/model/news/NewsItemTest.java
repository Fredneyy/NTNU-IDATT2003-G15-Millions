package ntnu.idatt2003.group15.model.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NewsItemTest {

  @Nested
  @DisplayName("Positive NewsItem Tests")
  class positiveNewsItemTests {

    @Test
    void constructorMapsFieldsInTheDeclaredOrder() {
      Instant when = Instant.parse("2026-05-23T10:15:30Z");
      NewsItem item = new NewsItem(
          "Big news",
          StockSectors.TECHNOLOGY,
          BigDecimal.valueOf(1.25),
          BigDecimal.valueOf(0.15),
          3,
          when,
          false);

      assertEquals("Big news", item.headline());
      assertEquals(StockSectors.TECHNOLOGY, item.sector());
      assertEquals(0, BigDecimal.valueOf(1.25).compareTo(item.volatility()));
      assertEquals(0, BigDecimal.valueOf(0.15).compareTo(item.changePercent()));
      assertEquals(3, item.durationUpdates());
      assertEquals(when, item.when());
      assertFalse(item.appliedChange());
    }

    @Test
    void reduceDurationDecrementsByOne() {
      NewsItem item = newItemWithDuration(3);
      item.reduceDuration();
      assertEquals(2, item.durationUpdates());
    }

    @Test
    void reduceDurationFloorsAtZero() {
      NewsItem item = newItemWithDuration(1);
      item.reduceDuration();
      item.reduceDuration();
      item.reduceDuration();
      assertEquals(0, item.durationUpdates());
    }

    @Test
    void isExpiredWhenDurationIsZero() {
      NewsItem item = newItemWithDuration(0);
      assertTrue(item.isExpired());
    }

    @Test
    void isNotExpiredWhilePositive() {
      NewsItem item = newItemWithDuration(2);
      assertFalse(item.isExpired());
    }

    @Test
    void footerTextDescribesRemainingDuration() {
      NewsItem item = newItemWithDuration(5);
      assertEquals("Volatility increased for 5 updates", item.footerText());
    }

    @Test
    void footerTextIsNullWhenExpired() {
      NewsItem item = newItemWithDuration(0);
      assertNull(item.footerText());
    }

    @Test
    void infoFactoryProducesExpiredItemWithOnlyHeadline() {
      NewsItem item = NewsItem.info("Welcome");

      assertEquals("Welcome", item.headline());
      assertNull(item.sector());
      assertNull(item.volatility());
      assertNull(item.changePercent());
      assertTrue(item.isExpired());
    }

    @Test
    void setAppliedChangeRoundTrips() {
      NewsItem item = newItemWithDuration(1);
      item.setAppliedChange(true);
      assertTrue(item.appliedChange());
      item.setAppliedChange(false);
      assertFalse(item.appliedChange());
    }
  }

  private static NewsItem newItemWithDuration(int duration) {
    return new NewsItem(
        "headline",
        StockSectors.TECHNOLOGY,
        BigDecimal.ONE,
        BigDecimal.ZERO,
        duration,
        Instant.now(),
        false);
  }
}
