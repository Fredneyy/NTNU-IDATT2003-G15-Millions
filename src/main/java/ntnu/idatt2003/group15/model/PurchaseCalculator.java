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

    public BigDecimal calculateCommission() {
        return calculateGross().multiply(BigDecimal.valueOf(0.005)); // 0.5%
    }

    public BigDecimal calculateTax() {
        return BigDecimal.valueOf(0);
    }

    public BigDecimal calculateTotal() {
        return calculateGross().add(calculateCommission()).add(calculateTax());
    }
}
