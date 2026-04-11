package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;

/**
 * Performs mathematical operations for financial transactions.
 */
public interface TransactionCalculator {
  BigDecimal calculateGross(Share share);
  BigDecimal calculateCommission(Share share, BigDecimal commission);
  BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax);

}
