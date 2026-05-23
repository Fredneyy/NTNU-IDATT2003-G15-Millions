package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InputValidatorTest {

  @Nested
  @DisplayName("isBigDecimalValuePositive")
  class isBigDecimalValuePositiveTests {

    @Test
    void positiveValueReturnsTrue() {
      assertTrue(InputValidator.isBigDecimalValuePositive(BigDecimal.valueOf(0.01)));
    }

    @Test
    void zeroReturnsFalse() {
      assertFalse(InputValidator.isBigDecimalValuePositive(BigDecimal.ZERO));
    }

    @Test
    void negativeValueReturnsFalse() {
      assertFalse(InputValidator.isBigDecimalValuePositive(BigDecimal.valueOf(-1)));
    }

    @Test
    void nullValueThrowsNullPointerException() {
      assertThrows(NullPointerException.class,
          () -> InputValidator.isBigDecimalValuePositive(null));
    }
  }

  @Nested
  @DisplayName("isInt")
  class isIntTests {

    @Test
    void parsablePositiveIntReturnsTrue() {
      assertTrue(InputValidator.isInt("42"));
    }

    @Test
    void parsableNegativeIntReturnsTrue() {
      assertTrue(InputValidator.isInt("-7"));
    }

    @Test
    void floatStringReturnsFalse() {
      assertFalse(InputValidator.isInt("3.14"));
    }

    @Test
    void emptyStringReturnsFalse() {
      assertFalse(InputValidator.isInt(""));
    }

    @Test
    void wordStringReturnsFalse() {
      assertFalse(InputValidator.isInt("abc"));
    }

    @Test
    void nullStringReturnsFalse() {
      assertFalse(InputValidator.isInt(null));
    }
  }
}
