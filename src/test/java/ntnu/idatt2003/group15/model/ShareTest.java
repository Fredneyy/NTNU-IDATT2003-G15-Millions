package ntnu.idatt2003.group15.model;

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

class ShareTest {

    @Nested
    @DisplayName("Positive Stock Tests")
    class positiveStockTests {
        private Stock stock;
        private Share share;
        @BeforeEach
        void setUp() {
            stock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100), 0.0, 0.0,
                List.of(StockSectors.TECHNOLOGY));
            share = new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        }

        @Test
        void getStock() {
            assertEquals(stock, share.stock());
        }

        @Test
        void getQuantity() {
            assertEquals(BigDecimal.valueOf(50), share.quantity());
        }

        @Test
        void getPricePerShareTimesQuantity() {
            assertEquals(0, share.pricePerShare().multiply(share.quantity()).compareTo(BigDecimal.valueOf(5000)));
        }

        @Test
        void getPricePerShare() {
            assertEquals(BigDecimal.valueOf(100), share.pricePerShare());
        }
    }

    @Nested
    @DisplayName("Negative Share Tests")
    class negativeShareTests {
        private Stock stock;

      @BeforeEach
        void setUp() {
            stock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100), 0.0, 0.0,
                List.of(StockSectors.TECHNOLOGY));
            Share share = new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        }

        @Test
        void nullStock() {
            assertThrows(NullPointerException.class, () -> {
                new Share(null, BigDecimal.valueOf(50), BigDecimal.valueOf(1000));
            });
        }

        @Test
        void zeroNullOrNegativeQuantity() {
            assertThrows(NullPointerException.class, () -> {
                new Share(stock, null, BigDecimal.valueOf(1000));
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new Share(stock, BigDecimal.valueOf(0), BigDecimal.valueOf(1000));
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new Share(stock, BigDecimal.valueOf(-100), BigDecimal.valueOf(1000));
            });
        }

        @Test
        void zeroNullOrNegativePricePerShare() {
            assertThrows(NullPointerException.class, () -> {
                new Share(stock, BigDecimal.valueOf(50), null);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(0));
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(-100));
            });
        }
    }
}