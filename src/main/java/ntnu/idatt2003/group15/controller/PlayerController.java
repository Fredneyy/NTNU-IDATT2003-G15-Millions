package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.Objects;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.player.PlayerStatus;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.transactions.TransactionArchive;

public class PlayerController {

  private final Player player;

  public PlayerController(Player player) {
    this.player = Objects.requireNonNull(player, "Player cannot be null");
  }

  public Player getPlayer() {
    return player;
  }

  public String getName() {
    return player.getName();
  }

  public ObjectProperty<BigDecimal> moneyProperty() {
    return player.moneyProperty();
  }

  public BigDecimal getMoney() {
    return player.getMoney();
  }

  public BigDecimal getStartingMoney() {
    return player.getStartingMoney();
  }

  public Portfolio getPortfolio() {
    return player.getPortfolio();
  }

  public TransactionArchive getTransactionArchive() {
    return player.getTransactionArchive();
  }

  public BigDecimal getNetWorth() {
    return player.getNetWorth();
  }

  public ObservableValue<BigDecimal> getCashProperty() {
    return player.moneyProperty();
  }

  public ObservableValue<BigDecimal> getNetWorthProperty() {
    return player.getNetWorthProperty();
  }

  public ObservableValue<BigDecimal> getNetWorthChangeProperty() {
    return player.getNetWorthChangeProperty();
  }

  public ObservableValue<BigDecimal> getNetWorthChangePercentProperty() {
    return player.getNetWorthChangePercentProperty();
  }

  public PlayerStatus getStatus(int week) {
    return player.getStatus(week);
  }
}
