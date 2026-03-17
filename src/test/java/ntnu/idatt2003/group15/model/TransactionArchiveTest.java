package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionArchiveTest {

  private Stock stock;
  private Share share;
  private Purchase purchase;
  private Sale sale;

  @BeforeEach
  void setUpShared() {
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(1000));
    purchase = new Purchase(share, 1);
    sale = new Sale(share, 2, BigDecimal.valueOf(110));
  }

  @Nested
  @DisplayName("Positive TransactionArchive Tests")
  class positiveTransactionArchiveTests {
    private TransactionArchive archive;

    @BeforeEach
    void setUp() {
      archive = new TransactionArchive();
    }

    @Test
    void isEmptyOnNewArchive() {
      assertTrue(archive.isEmpty());
    }

    @Test
    void isNotEmptyAfterAdd() {
      archive.add(purchase);
      assertFalse(archive.isEmpty());
    }

    @Test
    void addReturnsTrue() {
      assertTrue(archive.add(purchase));
    }

    @Test
    void getTransactionsByWeek() {
      archive.add(purchase);
      List<Transaction> result = archive.getTransactions(1);
      assertEquals(1, result.size());
      assertEquals(purchase, result.getFirst());
    }

    @Test
    void getTransactionsByWeekExcludesOtherWeeks() {
      archive.add(purchase);
      archive.add(sale);
      assertEquals(1, archive.getTransactions(1).size());
      assertEquals(1, archive.getTransactions(2).size());
    }

    @Test
    void getPurchasesByWeek() {
      archive.add(purchase);
      archive.add(sale);
      List<Purchase> purchases = archive.getPurchases(1);
      assertEquals(1, purchases.size());
      assertEquals(purchase, purchases.getFirst());
    }

    @Test
    void getSalesByWeek() {
      archive.add(purchase);
      archive.add(sale);
      List<Sale> sales = archive.getSales(2);
      assertEquals(1, sales.size());
      assertEquals(sale, sales.getFirst());
    }

    @Test
    void countDistinctWeeks() {
      archive.add(purchase);
      archive.add(sale);
      Purchase anotherPurchase = new Purchase(share, 1);
      archive.add(anotherPurchase);
      assertEquals(2, archive.countDistinctWeeks());
    }
  }

  @Nested
  @DisplayName("Negative TransactionArchive Tests")
  class negativeTransactionArchiveTests {
    private TransactionArchive archive;

    @BeforeEach
    void setUp() {
      archive = new TransactionArchive();
    }

    @Test
    void getTransactionsForEmptyWeekReturnsEmptyList() {
      archive.add(purchase);
      assertTrue(archive.getTransactions(99).isEmpty());
    }

    @Test
    void getPurchasesForEmptyWeekReturnsEmptyList() {
      assertTrue(archive.getPurchases(1).isEmpty());
    }

    @Test
    void getSalesForEmptyWeekReturnsEmptyList() {
      assertTrue(archive.getSales(1).isEmpty());
    }

    @Test
    void getPurchasesExcludesSales() {
      archive.add(sale);
      assertTrue(archive.getPurchases(2).isEmpty());
    }

    @Test
    void getSalesExcludesPurchases() {
      archive.add(purchase);
      assertTrue(archive.getSales(1).isEmpty());
    }

    @Test
    void countDistinctWeeksOnEmptyArchive() {
      assertEquals(0, archive.countDistinctWeeks());
    }
  }
}
