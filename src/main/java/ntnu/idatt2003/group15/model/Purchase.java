package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Purchase extends Transaction {
    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    @Override
    public void commit(Player player, BigDecimal commission, BigDecimal tax) throws NullPointerException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(commission, "Commission cannot be null");
        Objects.requireNonNull(tax, "Tax cannot be null");
        BigDecimal buyAmount = getCalculator().calculateTotal(commission, tax);
        player.withdrawMoney(buyAmount);
        player.getPortfolio().addShare(getShare());
        player.getTransactionArchive().add(this);
    }
}
