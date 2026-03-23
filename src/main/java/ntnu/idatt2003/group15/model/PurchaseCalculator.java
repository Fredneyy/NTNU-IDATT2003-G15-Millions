package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PurchaseCalculator implements TransactionCalculator {
    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;

    public PurchaseCalculator(Share share) throws NullPointerException {
        Objects.requireNonNull(share, "Share cannot be null");
        this.purchasePrice = share.getPricePerShare();
        this.quantity = share.getQuantity();
    }

    public BigDecimal calculateGross() {
        return purchasePrice.multiply(quantity);
    }

    public BigDecimal calculateCommission(BigDecimal commission) {
        return calculateGross().multiply(commission); // 0.5%
    }

    public BigDecimal calculateTax() {
        return BigDecimal.valueOf(0);
    }

    public BigDecimal calculateTotal(BigDecimal commission) {
        return calculateGross().add(calculateCommission(commission)).add(calculateTax());
    }
}
