package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCalculatorTest {

  // Share: quantity=10, purchasePrice=100
  private Stock stock;
  private Share share;

  @BeforeEach
  void setUpShared() {
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
  }

  @Nested
  @DisplayName("Positive PurchaseCalculator Tests")
  class positivePurchaseCalculatorTests {
    private PurchaseCalculator calculator;

    @BeforeEach
    void setUp() {
      calculator = new PurchaseCalculator(share);
    }

    @Test
    void calculateGross() {
      // purchasePrice(100) * quantity(10) = 1000
      assertEquals(0, calculator.calculateGross().compareTo(BigDecimal.valueOf(1000)));
    }

    @Test
    void calculateCommission() {
      // gross(1000) * 0.005 = 5
      assertEquals(0, calculator.calculateCommission().compareTo(BigDecimal.valueOf(5)));
    }

    @Test
    void calculateTax() {
      // purchases are not taxed
      assertEquals(0, calculator.calculateTax().compareTo(BigDecimal.valueOf(0)));
    }

    @Test
    void calculateTotal() {
      // gross(1000) + commission(5) + tax(0) = 1005
      assertEquals(0, calculator.calculateTotal().compareTo(BigDecimal.valueOf(1005)));
    }
  }

  @Nested
  @DisplayName("Negative PurchaseCalculator Tests")
  class negativePurchaseCalculatorTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new PurchaseCalculator(null)
      );
    }
  }

  @Nested
  @DisplayName("Positive SaleCalculator Tests")
  class positiveSaleCalculatorTests {
    // Share: quantity=10, purchasePrice=100, salesPrice=150
    private SaleCalculator calculator;

    @BeforeEach
    void setUp() {
      calculator = new SaleCalculator(share, BigDecimal.valueOf(150));
    }

    @Test
    void calculateGross() {
      // salesPrice(150) * quantity(10) = 1500
      assertEquals(0, calculator.calculateGross().compareTo(BigDecimal.valueOf(1500)));
    }

    @Test
    void calculateCommission() {
      // purchasePrice(100) * 0.01 = 1
      assertEquals(0, calculator.calculateCommission().compareTo(BigDecimal.valueOf(1)));
    }

    @Test
    void calculateTax() {
      // 0.3 * (gross(1500) - commission(1) - purchasePrice(100)*quantity(10))
      // 0.3 * (1500 - 1 - 1000) = 0.3 * 499 = 149.7
      assertEquals(0, calculator.calculateTax().compareTo(new BigDecimal("149.7")));
    }

    @Test
    void calculateTotal() {
      // gross(1500) - (commission(1) - tax(149.7)) = 1500 + 148.7 = 1648.7
      assertEquals(0, calculator.calculateTotal().compareTo(new BigDecimal("1648.7")));
    }
  }

  @Nested
  @DisplayName("Negative SaleCalculator Tests")
  class negativeSaleCalculatorTests {

    @Test
    void nullShareThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new SaleCalculator(null, BigDecimal.valueOf(150))
      );
    }

    @Test
    void nullSalesPriceThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () ->
          new SaleCalculator(share, null)
      );
    }
  }
}
