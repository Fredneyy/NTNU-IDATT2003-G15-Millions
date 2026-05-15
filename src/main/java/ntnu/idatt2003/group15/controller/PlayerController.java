package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.PlayerStatus;
import ntnu.idatt2003.group15.model.Portfolio;
import ntnu.idatt2003.group15.model.TransactionArchive;

public class PlayerController {

  private final Player player;

  public PlayerController(Player player) {
    this.player = Objects.requireNonNull(player, "Player cannot be null");
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

  public void addMoney(BigDecimal amount) {
    player.addMoney(amount);
  }

  public void withdrawMoney(BigDecimal amount) {
    player.withdrawMoney(amount);
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

  public ObjectProperty<PlayerStatus> statusProperty() {
    return player.statusProperty();
  }

  public PlayerStatus getStatus() {
    return player.getStatus();
  }
}
