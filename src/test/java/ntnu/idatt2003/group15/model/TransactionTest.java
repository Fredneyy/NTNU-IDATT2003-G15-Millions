package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

  private Share share;

  @BeforeEach
  void setUpShared() {
    Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100));
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
      BigDecimal expectedMoney = player.getMoney().subtract(purchase.getCalculator().calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO));
      purchase.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertEquals(0, player.getMoney().compareTo(expectedMoney));
    }

    @Test
    void commitAddsShareToPortfolio() {
      purchase.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertTrue(player.getPortfolio().contains(share));
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
      sale = new Sale(share, 2, BigDecimal.valueOf(150));
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
      BigDecimal expectedMoney = player.getMoney().add(sale.getCalculator().calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO));
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertEquals(0, player.getMoney().compareTo(expectedMoney));
    }

    @Test
    void commitRemovesShareFromPortfolio() {
      sale.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
      assertFalse(player.getPortfolio().contains(share));
    }
  }

  @Nested
  @DisplayName("Negative Sale Tests")
  class negativeSaleTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new Sale(null, 1, BigDecimal.valueOf(150))
      );
    }

    @Test
    void nullSalesPriceThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new Sale(share, 1, null)
      );
    }

    @Test
    void commitWithNullPlayerThrowsNullPointerException() {
      Sale sale = new Sale(share, 1, BigDecimal.valueOf(150));
      assertThrows(NullPointerException.class, () ->
          sale.commit(null, BigDecimal.ZERO, BigDecimal.ZERO)
      );
    }

    @Test
    void commitWithNullCommissionThrowsNullPointerException() {
      Sale sale = new Sale(share, 1, BigDecimal.valueOf(150));
      assertThrows(NullPointerException.class, () ->
          sale.commit(new Player("Ot", BigDecimal.ZERO), null, BigDecimal.ZERO)
      );
    }

    @Test
    void commitWithNullTaxThrowsNullPointerException() {
      Sale sale = new Sale(share, 1, BigDecimal.valueOf(150));
      assertThrows(NullPointerException.class, () ->
          sale.commit(new Player("Ot", BigDecimal.ZERO), BigDecimal.ZERO, null)
      );
    }

  }
}
