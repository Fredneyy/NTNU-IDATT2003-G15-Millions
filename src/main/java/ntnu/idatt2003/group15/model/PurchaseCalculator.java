package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PurchaseCalculator implements TransactionCalculator {

    public BigDecimal calculateGross(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        return share.getPricePerShare().multiply(share.getQuantity());
    }

    public BigDecimal calculateCommission(Share share, BigDecimal commission) {
        Objects.requireNonNull(share, "Share cannot be null");
        return calculateGross(share).multiply(commission);
    }

    public BigDecimal calculateTotal(Share share, BigDecimal commission, BigDecimal tax) {
        Objects.requireNonNull(share, "Share cannot be null");
        return calculateGross(share).add(calculateCommission(share, commission)).add(tax);
    }
}
