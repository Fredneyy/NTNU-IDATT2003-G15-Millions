package ntnu.idatt2003.group15.utilities;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Utility class for validating structural and domain constraints on input data.
 */
public class InputValidator {

  /**
   * Returns {@code true} if number is positive, {@code false} otherwise.
   *
   * @param price the amount
   * @return {@code true} if number is >0, {@code false} otherwise
   * @throws NullPointerException if price is null
   */
  public static boolean isBigDecimalValuePositive(BigDecimal price) throws NullPointerException {
    Objects.requireNonNull(price);
    return price.compareTo(java.math.BigDecimal.ZERO) <= 0;
  }
}
