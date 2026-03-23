package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleCalculator implements TransactionCalculator {

    public BigDecimal calculateGross(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        BigDecimal salesPrice = share.getStock().getSalesPrice();
        return salesPrice.multiply(share.getQuantity());
    }

    public BigDecimal calculateCommission(Share share, BigDecimal commission) {
        Objects.requireNonNull(share, "Share cannot be null");
        return share.getPricePerShare().multiply(commission);
    }

    public BigDecimal calculateTax(Share share, BigDecimal tax, BigDecimal commission) {
        Objects.requireNonNull(share, "Share cannot be null");
        return tax.multiply(calculateGross(share).subtract(calculateCommission(share, commission)));
    }

    public BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax) {
        Objects.requireNonNull(share, "Share cannot be null");
        return calculateGross(share).subtract(calculateCommission(share, commission)).subtract(calculateTax(share, tax, commission));
    }
}
