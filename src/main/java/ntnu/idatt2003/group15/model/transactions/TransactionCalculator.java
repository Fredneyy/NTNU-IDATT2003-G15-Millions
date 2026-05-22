package ntnu.idatt2003.group15.model.transactions;

import java.math.BigDecimal;
import ntnu.idatt2003.group15.model.stocks.Share;

/**
 * Performs mathematical operations for financial transactions.
 */
public interface TransactionCalculator {
  /**
   * Calculate gross proceeds.
   *
   * @param share the share
   * @return the big decimal gross proceeds
   */
  BigDecimal calculateGross(Share share);

  /**
   * Calculate commission from transaction.
   *
   * @param share      the share
   * @param commission the commission
   * @return the big decimal commission
   */
  BigDecimal calculateCommission(Share share, BigDecimal commission);

  /**
   * Calculate total price of the transaction.
   *
   * @param share      the share
   * @param commission the commission
   * @param tax        the tax
   * @return the big decimal total price
   */
  BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax);

}
