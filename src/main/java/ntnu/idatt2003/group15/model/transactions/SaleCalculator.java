package ntnu.idatt2003.group15.model.transactions;

import java.math.BigDecimal;
import java.util.Objects;

import javafx.beans.binding.ObjectExpression;
import ntnu.idatt2003.group15.model.stocks.Share;

/**
 * Calculates the financial outcomes and proceeds from selling stock.
 */
public class SaleCalculator implements TransactionCalculator {

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to sell
   * @return the value of the shares
   * @throws NullPointerException if any parameter is null
   */
  public BigDecimal calculateGross(Share share) throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    BigDecimal salesPrice = share.stock().getSalesPrice();
    return salesPrice.multiply(share.quantity());
  }

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to sell
   * @param commission the amount of comission
   * @return the amount of commission
   * @throws NullPointerException if any parameter is null   *
   */
  public BigDecimal calculateCommission(Share share, BigDecimal commission)
      throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(commission, "Commission cannot be null");
    return share.pricePerShare().multiply(commission).multiply(share.quantity());
  }

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to sell
   * @param tax the amount of tax
   * @param commission the amount of commission
   * @return the total amount of tax
   * @throws NullPointerException if any parameter is null
   */
  public BigDecimal calculateTax(Share share, BigDecimal tax, BigDecimal commission) throws
      NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(commission, "Commission cannot be null");
    Objects.requireNonNull(tax, "Tax cannot be null");
    BigDecimal taxableAmount = calculateProfit(share)
        .subtract(calculateCommission(share, commission));
    if (taxableAmount.signum() < 0) {
      return BigDecimal.ZERO;
    }
    return tax.multiply(taxableAmount);
  }

  /**
   * Computes and returns the  based on transaction data.
   *
   * @param share the share to sell
   * @param commission the amount of commission
   * @param tax the amount of tax
   * @return the total amount of sale minus taxes and commission
   * @throws NullPointerException if any parameter is null
   */
  public BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax) throws
      NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(commission, "Commission cannot be null");
    Objects.requireNonNull(tax, "Tax cannot be null");
    return calculateGross(share).subtract(calculateCommission(share, commission))
        .subtract(calculateTax(share, tax, commission));
  }

  private BigDecimal calculateProfit(Share share) {
    return share.stock().getSalesPrice().subtract(share.pricePerShare()).multiply(share.quantity());
  }
}
