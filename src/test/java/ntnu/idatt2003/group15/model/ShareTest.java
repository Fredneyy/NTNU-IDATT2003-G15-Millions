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

        @Test
        void sellReducesQuantity() {
            share.sell(BigDecimal.valueOf(20));
            assertEquals(0, BigDecimal.valueOf(30).compareTo(share.quantity()));
        }

        @Test
        void sellEntireHoldingLeavesNoQuantity() {
            // Selling all shares triggers the quantity validator's positive-value rule,
            // so the share is left in an invalid state on purpose — verify it throws.
            assertThrows(IllegalArgumentException.class, () -> share.sell(BigDecimal.valueOf(50)));
        }

        @Test
        void buyAtSamePriceKeepsPricePerShare() {
            share.buy(BigDecimal.valueOf(50), BigDecimal.valueOf(100));
            assertEquals(0, BigDecimal.valueOf(100).compareTo(share.pricePerShare()));
            assertEquals(0, BigDecimal.valueOf(100).compareTo(share.quantity()));
        }

        @Test
        void buyAtHigherPriceUpdatesWeightedAverage() {
            // 50 shares @ 100 + 50 shares @ 200 -> 100 shares @ 150
            share.buy(BigDecimal.valueOf(50), BigDecimal.valueOf(200));
            assertEquals(0, BigDecimal.valueOf(100).compareTo(share.quantity()));
            assertEquals(0, new BigDecimal("150.0000000000").compareTo(share.pricePerShare()));
        }

        @Test
        void quantityPropertyReflectsInitialValue() {
            assertEquals(0,
                BigDecimal.valueOf(50).compareTo(share.quantityProperty().get()));
        }

        @Test
        void quantityPropertyUpdatesAfterSell() {
            share.sell(BigDecimal.valueOf(20));
            assertEquals(0,
                BigDecimal.valueOf(30).compareTo(share.quantityProperty().get()));
        }

        @Test
        void quantityPropertyUpdatesAfterBuy() {
            share.buy(BigDecimal.valueOf(25), BigDecimal.valueOf(100));
            assertEquals(0,
                BigDecimal.valueOf(75).compareTo(share.quantityProperty().get()));
        }
    }

    @Nested
    @DisplayName("Negative Share Tests")
    class negativeShareTests {
        private Stock stock;
        private Share share;

      @BeforeEach
        void setUp() {
            stock = new Stock("AAPL", "Apple Inc", BigDecimal.valueOf(100), 0.0, 0.0,
                List.of(StockSectors.TECHNOLOGY));
            share = new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        }

        @Test
        void nullStock() {
            assertThrows(NullPointerException.class, () -> new Share(null, BigDecimal.valueOf(50), BigDecimal.valueOf(1000)));
        }

        @Test
        void zeroNullOrNegativeQuantity() {
            assertThrows(NullPointerException.class, () -> new Share(stock, null, BigDecimal.valueOf(1000)));
            assertThrows(IllegalArgumentException.class, () -> new Share(stock, BigDecimal.valueOf(0), BigDecimal.valueOf(1000)));
            assertThrows(IllegalArgumentException.class, () -> new Share(stock, BigDecimal.valueOf(-100), BigDecimal.valueOf(1000)));
        }

        @Test
        void zeroNullOrNegativePricePerShare() {
            assertThrows(NullPointerException.class, () -> new Share(stock, BigDecimal.valueOf(50), null));
            assertThrows(IllegalArgumentException.class, () -> new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(0)));
            assertThrows(IllegalArgumentException.class, () -> new Share(stock, BigDecimal.valueOf(50), BigDecimal.valueOf(-100)));
        }

        @Test
        void sellMoreThanOwnedThrows() {
            assertThrows(IllegalArgumentException.class, () -> share.sell(BigDecimal.valueOf(51)));
        }

        @Test
        void sellNegativeQuantityThrows() {
            assertThrows(IllegalArgumentException.class, () -> share.sell(BigDecimal.valueOf(-1)));
        }

        @Test
        void sellNullQuantityThrows() {
            assertThrows(NullPointerException.class, () -> share.sell(null));
        }

        @Test
        void buyWithNullQuantityThrows() {
            assertThrows(NullPointerException.class,
                () -> share.buy(null, BigDecimal.valueOf(100)));
        }

        @Test
        void buyWithNullPriceThrows() {
            assertThrows(NullPointerException.class,
                () -> share.buy(BigDecimal.valueOf(10), null));
        }

        @Test
        void buyWithNonPositiveQuantityThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> share.buy(BigDecimal.ZERO, BigDecimal.valueOf(100)));
        }

        @Test
        void buyWithNonPositivePriceThrows() {
            assertThrows(IllegalArgumentException.class,
                () -> share.buy(BigDecimal.valueOf(10), BigDecimal.ZERO));
        }
    }
}