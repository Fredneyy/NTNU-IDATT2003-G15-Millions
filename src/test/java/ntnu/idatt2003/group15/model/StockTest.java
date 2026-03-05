package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Nested
    @DisplayName("Positive Stock Tests")
    class positiveStockTests {
        private Stock appleStock;
        @BeforeEach
        void setUp() {
            appleStock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100));
        }

        @Test
        void getSymbol() {
            assertEquals("AAPL", appleStock.getSymbol());
        }

        @Test
        void getCompany() {
            assertEquals("Apple Inc", appleStock.getCompany());
        }

        @Test
        void getSalesPrice() {
            assertEquals(BigDecimal.valueOf(100), appleStock.getSalesPrice());
        }

        @Test
        void addNewSalesPrice() {
            appleStock.addNewSalesPrice(BigDecimal.valueOf(150));
            assertEquals(BigDecimal.valueOf(150), appleStock.getSalesPrice());
        }
    }

    @Nested
    @DisplayName("Negative Stock Tests")
    class negativeStockTests {
        private Stock appleStock;
        @BeforeEach
        void setUp() {
            appleStock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100));
        }

        @Test
        void nullOrEmptySymbol() {
            assertThrows(NullPointerException.class, () -> {
                new Stock(null, "Apple Inc", BigDecimal.valueOf(100));
            });
            assertThrows(BlankArgumentException.class, () -> {
                new Stock("", "Apple Inc", BigDecimal.valueOf(100));
            });
        }

        @Test
        void nullOrEmptyCompany() {
            assertThrows(NullPointerException.class, () -> {
                new Stock("AAPL", null, BigDecimal.valueOf(100));
            });
            assertThrows(BlankArgumentException.class, () -> {
                new Stock("AAPL", "", BigDecimal.valueOf(100));
            });
        }

        @Test
        void nullSalesPrice() {
            assertThrows(NullPointerException.class, () -> {
                new Stock("AAPL", "Apple Inc", null);
            });
        }

        @Test
        void zeroOrNegativeSalesPrice() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(0));
            });

            assertThrows(IllegalArgumentException.class, () -> {
                new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(-100));
            });
        }

        @Test
        void addZeroOrNegativeSalesPrice() {
            assertThrows(IllegalArgumentException.class, () -> {
                appleStock.addNewSalesPrice(BigDecimal.valueOf(0));
            });

            assertThrows(IllegalArgumentException.class, () -> {
                appleStock.addNewSalesPrice(BigDecimal.valueOf(-100));
            });
        }
    }
}