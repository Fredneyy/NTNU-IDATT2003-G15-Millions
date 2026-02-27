package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Player {

  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio = new Portfolio();
  private final TransactionArchive transactionArchive = new TransactionArchive();

  public Player(String name, BigDecimal startingMoney) {
      if (name == null || name.isBlank()) {
          throw new IllegalArgumentException("Name cannot be blank or null");
      }
      Objects.requireNonNull(startingMoney, "StartingMoney cannot be null");

    this.name = name;
    this.startingMoney = startingMoney;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  public void addMoney(BigDecimal amount) {
    money = money.add(amount);
  }

  public void withdrawMoney(BigDecimal amount) {
    money = money.subtract(amount);
  }

  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

}
