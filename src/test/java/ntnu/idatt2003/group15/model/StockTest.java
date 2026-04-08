package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Nested
    @DisplayName("Positive Stock Tests")
    class positiveStockTests {
        private Stock appleStock;
        @BeforeEach
        void setUp() {
            appleStock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100));
            appleStock.addNewSalesPrice(BigDecimal.valueOf(50));
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
            assertEquals(BigDecimal.valueOf(50), appleStock.getSalesPrice());
        }

        @Test
        void getHistoricalPrice() {
            List<BigDecimal> prices = appleStock.getHistoricalPrices();
            assertEquals(BigDecimal.valueOf(100), prices.getFirst());
        }

        @Test
        void getHighestPrice() {
            assertEquals(BigDecimal.valueOf(100), appleStock.getHighestPrice());
        }

        @Test
        void getLowestPrice() {
            assertEquals(BigDecimal.valueOf(50), appleStock.getLowestPrice());
        }

        @Test
        void addNewSalesPrice() {
            appleStock.addNewSalesPrice(BigDecimal.valueOf(150));
            assertEquals(BigDecimal.valueOf(150), appleStock.getSalesPrice());
        }

        @Test
        void getLatestPriceChange() {
            assertEquals(BigDecimal.valueOf(-50), appleStock.getLatestPriceChange());
            appleStock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100));
            assertEquals(BigDecimal.ZERO, appleStock.getLatestPriceChange());
        }

        @Test
        void getLatestPriceChangeRelative() {
            assertEquals(new BigDecimal("-0.50000"), appleStock.getLatestPriceChangeRelative());
        }

        @Test
        void setAndGetCategories() {
            List<String> categories = List.of("String");
            appleStock.setCategories(categories);
            assertEquals(categories, appleStock.getCategories());
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
        void addNegativeSalesPrice() {
            assertThrows(IllegalArgumentException.class, () -> {
                appleStock.addNewSalesPrice(BigDecimal.valueOf(-1));
            });

            assertThrows(IllegalArgumentException.class, () -> {
                appleStock.addNewSalesPrice(BigDecimal.valueOf(-100));
            });
        }
    }
}