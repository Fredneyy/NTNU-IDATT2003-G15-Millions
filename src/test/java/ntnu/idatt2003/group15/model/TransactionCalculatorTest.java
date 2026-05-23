package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.model.transactions.PurchaseCalculator;
import ntnu.idatt2003.group15.model.transactions.SaleCalculator;
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

    @Test
    void calculateCommissionWithNonZeroRate() {
      // gross(1000) * 0.01 = 10
      assertEquals(0,
          calculator.calculateCommission(share, new BigDecimal("0.01"))
              .compareTo(BigDecimal.valueOf(10)));
    }

    @Test
    void calculateTotalAddsFlatTaxOnTop() {
      // gross(1000) + commission(10) + tax(50) = 1060
      assertEquals(0,
          calculator.calculateTotal(share, new BigDecimal("0.01"), BigDecimal.valueOf(50))
              .compareTo(BigDecimal.valueOf(1060)));
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

    @Test
    void calculateCommissionRejectsNullShare() {
      PurchaseCalculator calculator = new PurchaseCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateCommission(null, BigDecimal.ZERO));
    }

    @Test
    void calculateTotalRejectsNullShare() {
      PurchaseCalculator calculator = new PurchaseCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateTotal(null, BigDecimal.ZERO, BigDecimal.ZERO));
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
      assertEquals(BigDecimal.ZERO, calculator.calculateCommission(saleShare, BigDecimal.ZERO));
    }

    @Test
    void calculateTax() {
      assertEquals(0, calculator.calculateTax(saleShare, BigDecimal.ZERO, BigDecimal.ZERO).compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculateTotal() {
      assertEquals(0, calculator.calculateTotal(saleShare, BigDecimal.ZERO, BigDecimal.ZERO).compareTo(new BigDecimal("10000")));
    }

    @Test
    void calculateCommissionWithNonZeroRate() {
      // pricePerShare(100) * commission(0.01) * quantity(10) = 10
      assertEquals(0,
          calculator.calculateCommission(saleShare, new BigDecimal("0.01"))
              .compareTo(BigDecimal.valueOf(10)));
    }

    @Test
    void calculateTaxOnProfitableSaleAppliesTaxRate() {
      // profit = (1000 - 100) * 10 = 9000; commission(0.01) = 10
      // taxable = 9000 - 10 = 8990; tax(0.37) * 8990 = 3326.30
      BigDecimal expected = new BigDecimal("3326.30");
      assertEquals(0,
          calculator.calculateTax(saleShare, new BigDecimal("0.37"), new BigDecimal("0.01"))
              .compareTo(expected));
    }

    @Test
    void calculateTaxOnLossReturnsZero() {
      // Sell below cost basis: profit is negative -> tax must be zero.
      Stock losingStock = new Stock("LOSS", "Loss Co", BigDecimal.valueOf(50), 0.0, 0.0,
          List.of(StockSectors.ENERGY));
      Share losingShare = new Share(losingStock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));

      assertEquals(0,
          calculator.calculateTax(losingShare, new BigDecimal("0.37"), BigDecimal.ZERO)
              .compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculateTotalSubtractsCommissionAndTax() {
      // gross(10000) - commission(10) - tax(3326.30) = 6663.70
      BigDecimal expected = new BigDecimal("6663.70");
      assertEquals(0,
          calculator.calculateTotal(saleShare, new BigDecimal("0.01"), new BigDecimal("0.37"))
              .compareTo(expected));
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

    @Test
    void calculateCommissionRejectsNullShare() {
      SaleCalculator calculator = new SaleCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateCommission(null, BigDecimal.ZERO));
    }

    @Test
    void calculateTaxRejectsNullShare() {
      SaleCalculator calculator = new SaleCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateTax(null, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void calculateTotalRejectsNullShare() {
      SaleCalculator calculator = new SaleCalculator();
      assertThrows(NullPointerException.class, () ->
          calculator.calculateTotal(null, BigDecimal.ZERO, BigDecimal.ZERO));
    }
  }
}
