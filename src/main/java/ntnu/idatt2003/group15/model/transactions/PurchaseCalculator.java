package ntnu.idatt2003.group15.model.transactions;

import java.math.BigDecimal;
import java.util.Objects;
import ntnu.idatt2003.group15.model.stocks.Share;

/**
 * Calculates the financial outcomes and costs of purchasing stock.
 */
public class PurchaseCalculator implements TransactionCalculator {

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to purchase
   * @return the price of the share
   */
  public BigDecimal calculateGross(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");
    return share.pricePerShare().multiply(share.quantity());
  }

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to purchase
   * @param commission the amount of commission
   * @return the total commission
   */
  public BigDecimal calculateCommission(Share share, BigDecimal commission) {
    Objects.requireNonNull(share, "Share cannot be null");
    return calculateGross(share).multiply(commission);
  }

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to purchase
   * @param commission the amount of commission
   * @param tax the amount of tax
   * @return the total amount needed to buy the share with commission and taxes on top
   */
  public BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax) {
    Objects.requireNonNull(share, "Share cannot be null");
    return calculateGross(share).add(calculateCommission(share, commission)).add(tax);
  }
}
