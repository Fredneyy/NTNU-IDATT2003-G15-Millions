package ntnu.idatt2003.group15.model.factories;

import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryTest {

  private Share testShare;
    private int testWeek;

    @BeforeEach
    void setUp() {
      Stock testStock = new Stock("EQNR", "Equinor ASA", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.ENERGY));
        testShare = new Share(testStock, BigDecimal.valueOf(3), BigDecimal.valueOf(100));
        testWeek = 10;
    }

    @Nested
    @DisplayName("Positive TransactionFactory Tests")
    class positiveTransactionFactoryTests {

        @Test
        void createPurchaseReturnsPurchaseInstance() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.PURCHASE, testShare, testWeek);
            assertInstanceOf(Purchase.class, transaction);
        }

        @Test
        void createSaleReturnsSaleInstance() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.SALE, testShare, testWeek);
            assertInstanceOf(Sale.class, transaction);
        }

        @Test
        void createPurchaseHasCorrectShare() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.PURCHASE, testShare, testWeek);
            assertEquals(testShare, transaction.getShare());
        }

        @Test
        void createSaleHasCorrectShare() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.SALE, testShare, testWeek);
            assertEquals(testShare, transaction.getShare());
        }

        @Test
        void createPurchaseHasCorrectWeek() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.PURCHASE, testShare, testWeek);
            assertEquals(testWeek, transaction.getWeek());
        }

        @Test
        void createSaleHasCorrectWeek() {
            Transaction transaction = TransactionFactory.createTransaction(TransactionType.SALE, testShare, testWeek);
            assertEquals(testWeek, transaction.getWeek());
        }
    }

    @Nested
    @DisplayName("Negative TransactionFactory Tests")
    class negativeTransactionFactoryTests {

        @Test
        void nullTypeThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                TransactionFactory.createTransaction(null, testShare, testWeek)
            );
        }

        @Test
        void nullShareThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                TransactionFactory.createTransaction(TransactionType.PURCHASE, null, testWeek)
            );
        }

        @Test
        void nullShareForSaleThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                TransactionFactory.createTransaction(TransactionType.SALE, null, testWeek)
            );
        }
    }
}
