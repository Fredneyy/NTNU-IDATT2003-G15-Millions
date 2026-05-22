package ntnu.idatt2003.group15.model.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.transactions.TransactionArchive;

/**
 * Represents a participant in the stock market simulation managing a portfolio and balance.
 */
public class Player {

  private final String name;
  private ObjectProperty<BigDecimal> money;
  private final BigDecimal startingMoney;
  private final Portfolio portfolio = new Portfolio();
  private final TransactionArchive transactionArchive = new TransactionArchive();
  private final ObservableValue<PlayerStatus> status;
  private final ObjectBinding<BigDecimal> netWorthBinding;
  private final ObjectBinding<BigDecimal> netWorthChangeBinding;
  private final ObjectBinding<BigDecimal> netWorthChangePercentBinding;

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

    ObservableValue<BigDecimal> marketValue = portfolio.getTotalMarketValueProperty();
    this.netWorthBinding = Bindings.createObjectBinding(
        () -> money.get().add(marketValue.getValue()),
        money, marketValue);
    this.netWorthChangeBinding = Bindings.createObjectBinding(
        () -> netWorthBinding.get().subtract(startingMoney),
        netWorthBinding);
    this.netWorthChangePercentBinding = Bindings.createObjectBinding(() -> {
      if (startingMoney.signum() == 0) return BigDecimal.ZERO;
      return netWorthChangeBinding.get()
          .divide(startingMoney, 4, RoundingMode.HALF_UP)
          .movePointRight(2);
    }, netWorthChangeBinding);
  }

  /** The balance the player started the session with — anchor for total-return calculations. */
  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  /** Observable cash balance, exposed as a read-only view of the money property. */
  public ObservableValue<BigDecimal> getCashProperty() {
    return money;
  }

  /** Observable net worth: cash + portfolio market value. */
  public ObservableValue<BigDecimal> getNetWorthProperty() {
    return netWorthBinding;
  }

  /** Observable change in net worth from starting balance. */
  public ObservableValue<BigDecimal> getNetWorthChangeProperty() {
    return netWorthChangeBinding;
  }

  /** Observable net worth change as a percent of the starting balance. */
  public ObservableValue<BigDecimal> getNetWorthChangePercentProperty() {
    return netWorthChangePercentBinding;
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
  public ObservableValue<PlayerStatus> statusProperty() {
    return status;
  }
}
