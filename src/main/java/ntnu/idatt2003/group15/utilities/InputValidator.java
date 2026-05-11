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
    return price.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Is text an integer
   *
   * @param text the text to check
   * @return {@code true} if text is an integer, {@code false} otherwise
   */
  public static boolean isInt(String text) {
    try {
      Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
}
