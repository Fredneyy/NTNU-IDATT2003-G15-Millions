package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleCalculator implements TransactionCalculator {
    private final BigDecimal purchasePrice;
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;

    public SaleCalculator(Share share, BigDecimal salesPrice) throws NullPointerException {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(salesPrice, "SalesPrice cannot be null");
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
        this.salesPrice = salesPrice;
    }

    public BigDecimal calculateGross() {
        return salesPrice.multiply(quantity);
    }

    public BigDecimal calculateCommission() {
        return purchasePrice.multiply(BigDecimal.valueOf(0.01)); // 1%
    }

    public BigDecimal calculateTax() {
        return BigDecimal.valueOf(0.3).multiply(calculateGross().subtract(calculateCommission()).subtract(purchasePrice.multiply(quantity)));
    }

    public BigDecimal calculateTotal() {
        return calculateGross().subtract(calculateCommission().subtract(calculateTax()));
    }
}
