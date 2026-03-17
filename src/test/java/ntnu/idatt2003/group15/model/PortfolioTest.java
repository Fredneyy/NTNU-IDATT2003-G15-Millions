package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class PortfolioTest {

  @Nested
  @DisplayName("Positive Portfolio Tests")
  class positivePortfolioTests {
    private Portfolio portfolio;
    private Share share;
    private Stock stock;

    @BeforeEach
    void setUp() {
      stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10));
      share = new Share(stock, BigDecimal.valueOf(100), stock.getSalesPrice());
      portfolio = new Portfolio();
    }

    @Test
    void addShare() {
      assertTrue(portfolio.addShare(share));
    }

    @Test
    void removeShare() {
      portfolio.addShare(share);
      assertTrue(portfolio.removeShare(share));
    }

    @Test
    void getShares() {
      portfolio.addShare(share);
      assertEquals(share.getStock().getCompany(),
          portfolio.getShares().getFirst().getStock().getCompany());
    }

    @Test
    void getSharesBySymbol() {
      portfolio.addShare(share);
      List<Share> result = portfolio.getShares("AAPL");
      assertEquals(1, result.size());
      assertEquals(share, result.getFirst());
    }

    @Test
    void getSharesBySymbolCaseInsensitive() {
      portfolio.addShare(share);
      List<Share> result = portfolio.getShares("aapl");
      assertEquals(1, result.size());
    }

    @Test
    void contains() {
      portfolio.addShare(share);
      assertTrue(portfolio.contains(share));
    }

    @Test
    void doesNotContainRemovedShare() {
      portfolio.addShare(share);
      portfolio.removeShare(share);
      assertFalse(portfolio.contains(share));
    }
  }

  @Nested
  @DisplayName("Negative Portfolio Tests")
  class negativePortfolioTests {
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
      portfolio = new Portfolio();
    }

    @Test
    void addNullShare() {
      assertThrows(NullPointerException.class, () ->
          portfolio.addShare(null)
      );
    }

    @Test
    void removeNullShare() {
      assertThrows(NullPointerException.class, () ->
          portfolio.removeShare(null)
      );
    }

    @Test
    void getSharesNullSymbol() {
      assertThrows(NullPointerException.class, () ->
          portfolio.getShares(null)
      );
    }

    @Test
    void containsNullShare() {
      assertThrows(NullPointerException.class, () ->
          portfolio.contains(null)
      );
    }

    @Test
    void removeShareNotInPortfolio() {
      Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10));
      Share share = new Share(stock, BigDecimal.valueOf(100), stock.getSalesPrice());
      assertFalse(portfolio.removeShare(share));
    }
  }
}
