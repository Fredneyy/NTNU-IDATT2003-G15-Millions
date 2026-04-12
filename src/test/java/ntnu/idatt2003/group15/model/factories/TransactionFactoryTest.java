package ntnu.idatt2003.group15.model.factories;

import ntnu.idatt2003.group15.model.Purchase;
import ntnu.idatt2003.group15.model.Sale;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryTest {

    private Stock testStock;
    private Share testShare;
    private int testWeek;

    @BeforeEach
    void setUp() {
        testStock = new Stock("EQNR", "Equinor ASA", BigDecimal.valueOf(100));
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
