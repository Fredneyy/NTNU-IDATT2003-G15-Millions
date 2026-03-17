package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Player {

    private final String name;
    private BigDecimal money;
    private final BigDecimal startingMoney;
    private final Portfolio portfolio = new Portfolio();
    private final TransactionArchive transactionArchive = new TransactionArchive();

    public Player(String name, BigDecimal startingMoney) throws BlankArgumentException, NullPointerException {
        Objects.requireNonNull(name, "Name cannot be zero");
        if (name.isBlank()) {
            throw new BlankArgumentException("Name cannot be blank");
        }
        Objects.requireNonNull(startingMoney, "StartingMoney cannot be null");

        this.name = name;
        this.startingMoney = startingMoney;
        this.money = startingMoney;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void addMoney(BigDecimal amount) throws NullPointerException {
        Objects.requireNonNull(amount);
        money = money.add(amount);
    }

    public void withdrawMoney(BigDecimal amount) throws NullPointerException {
        Objects.requireNonNull(amount);
        money = money.subtract(amount);
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public TransactionArchive getTransactionArchive() {
        return transactionArchive;
    }

}
