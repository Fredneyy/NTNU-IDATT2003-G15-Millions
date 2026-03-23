package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portfolio {
    private final List<Share> shares = new ArrayList<>();

    public boolean addShare(Share inputShare) throws NullPointerException {
        Objects.requireNonNull(inputShare, "Share cannot be null");
        return shares.add(inputShare);
    }

    public boolean removeShare(Share inputShare) throws NullPointerException {
        Objects.requireNonNull(inputShare, "Share cannot be null");
        return shares.remove(inputShare);
    }

    public List<Share> getShares() {
        return List.copyOf(shares);
    }

    public List<Share> getShares(String symbol) throws NullPointerException {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        return shares.stream()
                .filter(share -> share.getStock().getSymbol().equalsIgnoreCase(symbol))
                .toList();
    }

    public boolean contains(Share inputShare) throws NullPointerException {
        Objects.requireNonNull(inputShare, "Share cannot be null");
        return shares.contains(inputShare);
    }

    public BigDecimal getTotalMarketValue() {
        SaleCalculator saleCalculator = new SaleCalculator();
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Share currentShare : shares) {
            totalValue = totalValue.add(saleCalculator.calculateGross(currentShare));
        }
        return totalValue;
    }
}
