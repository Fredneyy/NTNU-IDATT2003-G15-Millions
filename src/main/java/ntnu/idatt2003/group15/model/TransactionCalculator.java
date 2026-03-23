package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;

public interface TransactionCalculator {
  BigDecimal calculateGross();
  BigDecimal calculateCommission(BigDecimal commission);
  BigDecimal calculateTax(BigDecimal tax);
  BigDecimal calculateTotal(BigDecimal commission, BigDecimal tax);

}
