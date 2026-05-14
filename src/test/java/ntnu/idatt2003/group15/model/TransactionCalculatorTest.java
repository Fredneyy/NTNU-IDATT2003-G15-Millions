package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCalculatorTest {

  private Stock stock;
  private Share share;

  @BeforeEach
  void setUpShared() {
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
  }

  @Nested
  @DisplayName("Positive PurchaseCalculator Tests")
  class positivePurchaseCalculatorTests {
    private PurchaseCalculator calculator;

    @BeforeEach
    void setUp() {
      calculator = new PurchaseCalculator();
    }

    @Test
    void calculateGross() {
      // purchasePrice(100) * quantity(10) = 1000
      assertEquals(0, calculator.calculateGross(share).compareTo(BigDecimal.valueOf(1000)));
    }

    @Test
    void calculateCommission() {
      // gross(1000) * 0 = 0
      assertEquals(0, calculator.calculateCommission(share, BigDecimal.ZERO).compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void calculateTotal() {
      // gross(1000) + commission(5) + tax(0) = 1005
      assertEquals(0, calculator.calculateTotal(share, new BigDecimal("0.005"), BigDecimal.ZERO).compareTo(BigDecimal.valueOf(1005)));
    }
  }

  @Nested
  @DisplayName("Negative PurchaseCalculator Tests")
  class negativePurchaseCalculatorTests {

    @Test
    void nullShareThrowsNullPointerException() {
      PurchaseCalculator calculator = new PurchaseCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateGross(null)
      );
    }
  }

  @Nested
  @DisplayName("Positive SaleCalculator Tests")
  class positiveSaleCalculatorTests {
    private SaleCalculator calculator;
    private Share saleShare;

    @BeforeEach
    void setUp() {
      calculator = new SaleCalculator();
      stock.addNewSalesPrice(BigDecimal.valueOf(1000));
      saleShare = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
    }

    @Test
    void calculateGross() {
      assertEquals(0, calculator.calculateGross(saleShare).compareTo(BigDecimal.valueOf(10000)));
    }

    @Test
    void calculateCommission() {
      assertEquals(0, calculator.calculateCommission(saleShare, new BigDecimal("0.01")).compareTo(BigDecimal.valueOf(1)));
    }

    @Test
    void calculateTax() {
      assertEquals(0, calculator.calculateTax(saleShare, BigDecimal.ZERO, BigDecimal.ZERO).compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculateTotal() {
      assertEquals(0, calculator.calculateTotal(saleShare, BigDecimal.ZERO, BigDecimal.ZERO).compareTo(new BigDecimal("10000")));
    }
  }

  @Nested
  @DisplayName("Negative SaleCalculator Tests")
  class negativeSaleCalculatorTests {

    @Test
    void nullShareThrowsNullPointerException() {
      SaleCalculator calculator = new SaleCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateGross(null)
      );
    }
  }
}
