package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Sale extends Transaction {
    public Sale(Share share, int week) {
        super(share, week, new SaleCalculator());
    }

    @Override
    public void commit(Player player, BigDecimal commission, BigDecimal tax) throws NullPointerException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(commission, "Commission cannot be null");
        Objects.requireNonNull(tax, "Tax cannot be null");
        BigDecimal saleAmount = getCalculator().calculateTotal(getShare(), commission, tax);
        player.addMoney(saleAmount);
        player.getPortfolio().removeShare(getShare());
        player.getTransactionArchive().add(this);
        setCommitted(true);
    }
}
