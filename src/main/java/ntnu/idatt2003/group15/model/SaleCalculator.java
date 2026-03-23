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
        this.purchasePrice = share.getPricePerShare();
        this.quantity = share.getQuantity();
        this.salesPrice = salesPrice;
    }

    public BigDecimal calculateGross() {
        return salesPrice.multiply(quantity);
    }

    public BigDecimal calculateCommission(BigDecimal commission) {
        return purchasePrice.multiply(commission); // 1%
    }

    public BigDecimal calculateTax(BigDecimal tax, BigDecimal commission) {
        return tax.multiply(calculateGross().subtract(calculateCommission(commission)));
    }

    public BigDecimal calculateTotal(BigDecimal commission, BigDecimal tax) {
        return calculateGross().subtract(calculateCommission(commission).subtract(calculateTax(tax, commission)));
    }
}
