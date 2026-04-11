package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Represents a participant in the stock market simulation managing a portfolio and balance.
 */
public class Player {

  private final String name;
  private ObjectProperty<BigDecimal> money;
  private final BigDecimal startingMoney;
  private final Portfolio portfolio = new Portfolio();
  private final TransactionArchive transactionArchive = new TransactionArchive();
  private ObjectProperty<PlayerStatus> status;

  /**
   * Initializes a new player with a name and starting balance.
   *
   * @param name the name of the player
   * @param startingMoney the amount of money to start with
   */
  public Player(String name, BigDecimal startingMoney)
      throws BlankArgumentException, NullPointerException {
    Objects.requireNonNull(name, "Name cannot be zero");
    if (name.isBlank()) {
      throw new BlankArgumentException("Name cannot be blank");
    }
    Objects.requireNonNull(startingMoney, "StartingMoney cannot be null");

    this.name = name;
    this.startingMoney = startingMoney;
    this.money = new SimpleObjectProperty<>(startingMoney);
    this.status = new SimpleObjectProperty<>(PlayerStatus.NOVICE);
  }

  /**
   * Returns the participant's name.
   *
   * @return the name of the player
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the observable property for the player's balance.
   *
   * @return the money property
   */
  public ObjectProperty<BigDecimal> moneyProperty() {
    return money;
  }

  /**
   * Returns the current balance of the player.
   *
   * @return the amount of money
   */
  public BigDecimal getMoney() {
    return money.get();
  }

  /**
   * Deposits the specified amount to the player's balance.
   *
   * @param amount the amount to add to money
   */
  public void addMoney(BigDecimal amount) throws NullPointerException {
    Objects.requireNonNull(amount);
    BigDecimal newMoney = money.get().add(amount);
    money.set(newMoney);
  }

  /**
   * Withdraws the specified amount from the player's balance.
   *
   * @param amount the amount to withdraw from money
   */
  public void withdrawMoney(BigDecimal amount) throws NullPointerException {
    Objects.requireNonNull(amount);
    BigDecimal newMoney = money.get().subtract(amount);
    money.set(newMoney);
  }

  /**
   * Retrieves the player's current portfolio of active holdings.
   *
   * @return the portfolio of the player
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  /**
   * Retrieves the transaction archive recording the player's history.
   *
   * @return the transaction archive of the player
   */
  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  /**
   * Calculates the player's total net worth including liquid balance and stock market value.
   *
   * @return the current net worth of the player
   */
  public BigDecimal getNetWorth() {
    BigDecimal marketValue = getPortfolio().getTotalMarketValue();
    return marketValue.add(money.get());
  }

  /**
   * Returns the observable property for the player's status.
   *
   * @return the status property of the player
   */
  public ObjectProperty<PlayerStatus> statusProperty() {
    return status;
  }

  /**
   * Returns the current status of the player.
   *
   * @return the current status of the player
   */
  public PlayerStatus getStatus() {
    return status.get();
  }
}
