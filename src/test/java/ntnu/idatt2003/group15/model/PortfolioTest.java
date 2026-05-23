package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
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
      stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
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
      assertEquals(share.stock().getCompany(),
          portfolio.getShares().getFirst().stock().getCompany());
    }

    @Test
    void getSharesBySymbol() {
      portfolio.addShare(share);
      Share result = portfolio.getShare("AAPL");
      assertEquals(share, result);
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

    @Test
    void getTotalMarketValue() {
      portfolio.addShare(share);
      BigDecimal totalValue = portfolio.getTotalMarketValue();
      assertEquals(BigDecimal.valueOf(1000), totalValue);
    }

    @Test
    void investedPropertyIsZeroForEmptyPortfolio() {
      assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getInvestedProperty().getValue()));
    }

    @Test
    void investedPropertyReflectsCostBasis() {
      portfolio.addShare(share); // 100 shares at price 10 = 1000
      assertEquals(0,
          BigDecimal.valueOf(1000).compareTo(portfolio.getInvestedProperty().getValue()));
    }

    @Test
    void investedPropertyStaysAtCostBasisWhenPriceChanges() {
      portfolio.addShare(share);
      stock.addNewSalesPrice(BigDecimal.valueOf(20));
      // cost basis locked at purchase price; should still be 1000
      assertEquals(0,
          BigDecimal.valueOf(1000).compareTo(portfolio.getInvestedProperty().getValue()));
    }

    @Test
    void unrealizedPnlIsZeroWhenPriceUnchanged() {
      portfolio.addShare(share);
      assertEquals(0,
          BigDecimal.ZERO.compareTo(portfolio.getUnrealizedPnlProperty().getValue()));
    }

    @Test
    void unrealizedPnlReflectsPriceIncrease() {
      portfolio.addShare(share);
      stock.addNewSalesPrice(BigDecimal.valueOf(15)); // +5 per share, 100 shares -> +500
      assertEquals(0,
          BigDecimal.valueOf(500).compareTo(portfolio.getUnrealizedPnlProperty().getValue()));
    }

    @Test
    void unrealizedPnlReflectsPriceDecrease() {
      portfolio.addShare(share);
      stock.addNewSalesPrice(BigDecimal.valueOf(8)); // -2 per share, 100 shares -> -200
      assertEquals(0,
          BigDecimal.valueOf(-200).compareTo(portfolio.getUnrealizedPnlProperty().getValue()));
    }

    @Test
    void unrealizedPnlPercentIsZeroForEmptyPortfolio() {
      assertEquals(0,
          BigDecimal.ZERO.compareTo(portfolio.getUnrealizedPnlPercentProperty().getValue()));
    }

    @Test
    void unrealizedPnlPercentReflectsGains() {
      portfolio.addShare(share);
      stock.addNewSalesPrice(BigDecimal.valueOf(15)); // +50% on cost basis
      assertEquals(0,
          new BigDecimal("50.00")
              .compareTo(portfolio.getUnrealizedPnlPercentProperty().getValue()));
    }

    @Test
    void totalMarketValuePropertyReactsToPriceChange() {
      // The property goes through ObjectBinding/ListChangeListener — a separate
      // code path from getTotalMarketValue() which recomputes eagerly on call.
      portfolio.addShare(share);
      assertEquals(0,
          BigDecimal.valueOf(1000).compareTo(portfolio.getTotalMarketValueProperty().getValue()));

      stock.addNewSalesPrice(BigDecimal.valueOf(25)); // 100 shares * 25 = 2500

      assertEquals(0,
          BigDecimal.valueOf(2500).compareTo(portfolio.getTotalMarketValueProperty().getValue()));
    }

    @Test
    void totalMarketValuePropertyReactsToShareRemoval() {
      portfolio.addShare(share);
      portfolio.removeShare(share);

      assertEquals(0,
          BigDecimal.ZERO.compareTo(portfolio.getTotalMarketValueProperty().getValue()));
    }

    @Test
    void getListPropertyReflectsAddedShares() {
      assertTrue(portfolio.getListProperty().isEmpty());
      portfolio.addShare(share);
      assertEquals(1, portfolio.getListProperty().size());
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
          portfolio.getShare(null)
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
      Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(stock, BigDecimal.valueOf(100), stock.getSalesPrice());
      assertFalse(portfolio.removeShare(share));
    }
  }
}
