package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

  private Share share;

  @BeforeEach
  void setUpShared() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
  }

  // Transaction is abstract — base behaviour is tested through Purchase

  @Nested
  @DisplayName("Positive Transaction Tests")
  class positiveTransactionTests {
    private Purchase transaction;

    @BeforeEach
    void setUp() {
      transaction = new Purchase(share, 3);
    }

    @Test
    void getShare() {
      assertEquals(share, transaction.getShare());
    }

    @Test
    void getWeek() {
      assertEquals(3, transaction.getWeek());
    }

    @Test
    void getCalculatorIsNotNull() {
      assertNotNull(transaction.getCalculator());
    }

    @Test
    void isNotCommittedByDefault() {
      assertFalse(transaction.isCommitted());
    }

    @Test
    void getCommittedAtIsNotNullByDefault() {
      // Transactions stamp Instant.now() at construction.
      assertNotNull(transaction.getCommittedAt());
    }
  }

  @Nested
  @DisplayName("Negative Transaction Tests")
  class negativeTransactionTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new Purchase(null, 1)
      );
    }
  }

  @Nested
  @DisplayName("Positive Purchase Tests")
  class positivePurchaseTests {
    private Purchase purchase;
    private Player player;

    @BeforeEach
    void setUp() {
      purchase = new Purchase(share, 1);
      player = new Player("trader", BigDecimal.valueOf(10000));
    }

    @Test
    void getShareReturnsPurchasedShare() {
      assertEquals(share, purchase.getShare());
    }

    @Test
    void getWeekReturnsPurchaseWeek() {
      assertEquals(1, purchase.getWeek());
    }

    @Test
    void commitWithdrawsMoneyFromPlayer() {
      BigDecimal expectedMoney = player.getMoney().subtract(purchase.getCalculator().calculateTotal(share, BigDecimal.ZERO, BigDecimal.ZERO));
      purchase.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertEquals(0, player.getMoney().compareTo(expectedMoney));
    }

    @Test
    void commitAddsShareToPortfolio() {
      purchase.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertTrue(player.getPortfolio().contains(share));
    }

    @Test
    void restoredPurchaseIsCommittedWithGivenWeekAndTimestamp() {
      Instant when = Instant.parse("2026-05-23T10:15:30Z");
      Purchase restored = Purchase.restored(share, 4, when);

      assertTrue(restored.isCommitted());
      assertEquals(4, restored.getWeek());
      assertEquals(when, restored.getCommittedAt());
    }

    @Test
    void restoredPurchaseWithNullCommittedAtKeepsDefaultInstant() {
      Purchase restored = Purchase.restored(share, 1, null);
      assertNotNull(restored.getCommittedAt());
    }
  }

  @Nested
  @DisplayName("Negative Purchase Tests")
  class negativePurchaseTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new Purchase(null, 1)
      );
    }

    @Test
    void commitWithNullPlayerThrowsNullPointerException() {
      Purchase purchase = new Purchase(share, 1);
      assertThrows(NullPointerException.class, () ->
          purchase.commit(null, BigDecimal.ZERO, BigDecimal.ZERO)
      );
    }
  }

  @Nested
  @DisplayName("Positive Sale Tests")
  class positiveSaleTests {
    private Sale sale;
    private Player player;

    @BeforeEach
    void setUp() {
      sale = new Sale(share, 2);
      player = new Player("trader", BigDecimal.valueOf(10000));
      player.getPortfolio().addShare(share);
    }

    @Test
    void getShareReturnsSoldShare() {
      assertEquals(share, sale.getShare());
    }

    @Test
    void getWeekReturnsSaleWeek() {
      assertEquals(2, sale.getWeek());
    }

    @Test
    void commitAddsMoneyToPlayer() {
      BigDecimal expectedMoney = player.getMoney().add(sale.getCalculator().calculateTotal(share, BigDecimal.ZERO, BigDecimal.ZERO));
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertEquals(0, player.getMoney().compareTo(expectedMoney));
    }

    @Test
    void commitRemovesShareFromPortfolio() {
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertFalse(player.getPortfolio().contains(share));
    }

    @Test
    void getSalePricePerShareIsNullBeforeCommit() {
      assertNull(sale.getSalePricePerShare());
    }

    @Test
    void getProceedsIsNullBeforeCommit() {
      assertNull(sale.getProceeds());
    }

    @Test
    void getRealizedPnlIsZeroBeforeCommit() {
      assertEquals(0, BigDecimal.ZERO.compareTo(sale.getRealizedPnl()));
    }

    @Test
    void commitPopulatesSalePriceAndProceeds() {
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertNotNull(sale.getSalePricePerShare());
      assertNotNull(sale.getProceeds());
    }

    @Test
    void getRealizedPnlIsPositiveWhenPriceRose() {
      // share was bought at price 100, quantity 10 -> cost basis 1000.
      // Stock has already had its price set to 100. Bump to 150 and sell.
      share.stock().addNewSalesPrice(BigDecimal.valueOf(150));
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      // gross = 150 * 10 = 1500; no tax/commission; cost basis = 1000; pnl = 500.
      assertEquals(0, BigDecimal.valueOf(500).compareTo(sale.getRealizedPnl()));
    }

    @Test
    void getRealizedPnlIsNegativeWhenPriceFell() {
      share.stock().addNewSalesPrice(BigDecimal.valueOf(60));
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      // gross = 60 * 10 = 600; cost basis = 1000; pnl = -400.
      assertEquals(0, BigDecimal.valueOf(-400).compareTo(sale.getRealizedPnl()));
    }

    @Test
    void restoredSaleIsCommittedWithGivenState() {
      Instant when = Instant.parse("2026-05-23T10:15:30Z");
      Sale restored = Sale.restored(share, 7, when,
          BigDecimal.valueOf(120), BigDecimal.valueOf(1180));

      assertTrue(restored.isCommitted());
      assertEquals(7, restored.getWeek());
      assertEquals(when, restored.getCommittedAt());
      assertEquals(0, BigDecimal.valueOf(120).compareTo(restored.getSalePricePerShare()));
      assertEquals(0, BigDecimal.valueOf(1180).compareTo(restored.getProceeds()));
    }

    @Test
    void restoredSaleWithNullCommittedAtKeepsDefaultInstant() {
      Sale restored = Sale.restored(share, 1, null,
          BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
      assertNotNull(restored.getCommittedAt());
    }
  }

  @Nested
  @DisplayName("Negative Sale Tests")
  class negativeSaleTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new Sale(null, 1)
      );
    }

    @Test
    void commitWithNullPlayerThrowsNullPointerException() {
      Sale sale = new Sale(share, 1);
      assertThrows(NullPointerException.class, () ->
          sale.commit(null, BigDecimal.ZERO, BigDecimal.ZERO)
      );
    }

    @Test
    void commitWithNullCommissionThrowsNullPointerException() {
      Sale sale = new Sale(share, 1);
      assertThrows(NullPointerException.class, () ->
          sale.commit(new Player("Ot", BigDecimal.ZERO), null, BigDecimal.ZERO)
      );
    }

    @Test
    void commitWithNullTaxThrowsNullPointerException() {
      Sale sale = new Sale(share, 1);
      assertThrows(NullPointerException.class, () ->
          sale.commit(new Player("Ot", BigDecimal.ZERO), BigDecimal.ZERO, null)
      );
    }

  }
}
