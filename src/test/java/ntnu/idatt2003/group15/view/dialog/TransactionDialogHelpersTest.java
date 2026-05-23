package ntnu.idatt2003.group15.view.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Direct tests for the static helpers on {@link TransactionDialog}. They're
 * {@code protected static} so we reach them from the same package, no JavaFX
 * runtime needed for these pure functions.
 */
class TransactionDialogHelpersTest {

  @Test
  void parseQuantityOnNullReturnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(TransactionDialog.parseQuantity(null)));
  }

  @Test
  void parseQuantityOnBlankReturnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(TransactionDialog.parseQuantity("   ")));
  }

  @Test
  void parseQuantityOnValidNumberReturnsThatNumber() {
    assertEquals(0, new BigDecimal("42").compareTo(TransactionDialog.parseQuantity("42")));
    assertEquals(0,
        new BigDecimal("3.5").compareTo(TransactionDialog.parseQuantity(" 3.5 ")));
  }

  @Test
  void parseQuantityClampsNegativeToZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(TransactionDialog.parseQuantity("-10")));
  }

  @Test
  void parseQuantityOnNonNumericReturnsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(TransactionDialog.parseQuantity("abc")));
  }

  @Test
  void formatMoneyOnNullReturnsDefaultZero() {
    assertEquals("$0.00", TransactionDialog.formatMoney(null));
  }

  @Test
  void formatMoneyOnPositiveValueRoundsToTwoDecimals() {
    assertEquals("$1234.57", TransactionDialog.formatMoney(new BigDecimal("1234.567")));
  }

  @Test
  void formatMoneyOnZeroFormatsWithoutSign() {
    assertEquals("$0.00", TransactionDialog.formatMoney(BigDecimal.ZERO));
  }

  @Test
  void formatMoneyOnNegativePrependsMinusSign() {
    assertEquals("-$50.25", TransactionDialog.formatMoney(new BigDecimal("-50.25")));
  }
}
